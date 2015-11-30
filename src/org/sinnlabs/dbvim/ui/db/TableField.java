/**
 * 
 */
package org.sinnlabs.dbvim.ui.db;

import static org.zkoss.lang.Generics.cast;

import java.util.AbstractSequentialList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;

import org.sinnlabs.dbvim.config.ConfigLoader;
import org.sinnlabs.dbvim.db.Database;
import org.sinnlabs.dbvim.db.DatabaseFactory;
import org.sinnlabs.dbvim.db.Entry;
import org.sinnlabs.dbvim.db.Value;
import org.sinnlabs.dbvim.db.exceptions.DatabaseOperationException;
import org.sinnlabs.dbvim.evaluator.AbstractVariableSet;
import org.sinnlabs.dbvim.evaluator.DatabaseConditionBuilder;
import org.sinnlabs.dbvim.evaluator.exceptions.ParseException;
import org.sinnlabs.dbvim.form.FormFieldResolver;
import org.sinnlabs.dbvim.form.FormFieldResolverFactory;
import org.sinnlabs.dbvim.model.Form;
import org.sinnlabs.dbvim.script.Record;
import org.sinnlabs.dbvim.ui.IField;
import org.sinnlabs.dbvim.ui.annotations.EventType;
import org.sinnlabs.dbvim.ui.annotations.WireEvent;
import org.sinnlabs.dbvim.zk.model.IFormComposer;
import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.UiException;
import org.zkoss.zk.ui.event.CreateEvent;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Idspace;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;

import com.mysql.jdbc.StringUtils;

/**
 * Class represents Table field
 * @author peter.liverovsky
 *
 */
public class TableField extends Idspace {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2657721752963776218L;
	
	protected String formName;
	
	protected String qualification;
	
	protected Form form;
	
	protected FormFieldResolver resolver;
	
	protected IFormComposer composer;
	
	protected List<IField<?>> selectFields;
	
	protected Database db;
	
	protected boolean isChildable = true;
	
	private transient List<TableColumnField> _items;
	
	private int _hdcnt = 1;
	
	@Wire
	private Listbox lstData;
	
	/* gatters and setters */
	public String getFormName() { return formName; }
	public void setFormName(String form) throws Exception { 
		formName = form;
		loadForm();
	}
	public String getQualification() { return qualification; }
	public void setQualification(String q) { qualification = q; }
	
	
	public List<TableColumnField> getTableColumns() { return _items; } 
	
	public TableField() {
		super();
		
		// initialize component after all attributes are loaded
		addEventListener(Events.ON_CREATE, new EventListener<CreateEvent>() {

			@Override
			public void onEvent(CreateEvent e) throws Exception {
				onCreate(e.getArg());
			}

		});
		
		selectFields = new ArrayList<IField<?>>();
		
		/* get the composer */
		if (Executions.getCurrent().getArg() != null) {
			@SuppressWarnings("unchecked")
			Map<String, Object> args = (Map<String, Object>) Executions.getCurrent().getArg();
			Object c = args.get("composer");
			if ( c!= null)
				composer = (IFormComposer) c;
		}
		
		/* Create the ui */
		isChildable = true;
		Executions.createComponents("/components/tablefield.zul", this, null);
		Selectors.wireVariables(this, this, null);
		Selectors.wireComponents(this, this, false);
		Selectors.wireEventListeners(this, this);

		// lock the component
		isChildable = false;
		
		init();
	}
	
	

	@WireEvent(EventType.CHANGE_FORM_MODE)
	public void setFieldMode(int mode) {
		lstData.getItems().clear();
	}
	
	@WireEvent(EventType.FORM_LOADED)
	public void onFormLoaded() throws Exception {
		// initialize resolver and database object
		if (form == null)
			return;
		if (resolver == null) {
			resolver = FormFieldResolverFactory.getResolver(form);
			db = DatabaseFactory.createInstance(form, resolver);
		}
		for (TableColumnField column : _items) {
			IField<?> f = resolver.getFields().get(column.getField());
			if (f == null) {
				throw new IllegalArgumentException("Cannot find field id: " + column.getField() + 
						" on form: " + form);
			}
			selectFields.add(f);
		}
		if (qualification == null)
			qualification= "";
	}
	
	@WireEvent(EventType.ENTRY_LOADED)
	public void onEntryLoaded(Entry e) throws ParseException, DatabaseOperationException {
		loadData();
	}
	
	@Listen("onClick = #btnRefresh")
	public void refreshTable() throws ParseException, DatabaseOperationException {
		loadData();
	}
	
	private void loadData() throws ParseException, DatabaseOperationException {
		lstData.getItems().clear();
		AbstractVariableSet<Value<?>> variables = 
				DatabaseConditionBuilder.buildVariablesFromFields(composer.getFields());
		
		List<Entry> entries = db.query(selectFields, qualification, 0, variables);
		
		for (Entry entry : entries) {
			Listitem item = new Listitem();
			for (Value<?> v : entry.getValues()) {
				if (v.getValue() != null)
					item.appendChild(new Listcell(v.getValue().toString()));
				else
					item.appendChild(new Listcell(""));
			}
			item.setValue(entry);
			lstData.getItems().add(item);
		}
	}
	
	/**
	 * Returns table records
	 * @return
	 */
	public List<Record> getRecords() {
		List<Record> records = new ArrayList<Record>(lstData.getItemCount());
		for(Listitem i : lstData.getItems()) {
			Entry e = i.getValue();
			Record r = new Record();
			for (int k=0; k<e.getValues().size(); k++) {
				r.getValues().put(selectFields.get(k).getId(), e.getValues().get(k).getValue());
			}
			records.add(r);
		}
		return records;
	}
	
	@Override
	public void beforeChildAdded(Component child, Component refChild) {
		// if component is locked
		if (!isChildable) {
			if (!(child instanceof TableColumnField))
				throw new UiException("Wrong child: " + child);
		}
		super.beforeChildAdded(child, refChild);
	}
	
	@Override
	public boolean insertBefore(Component child, Component insertBefore) {
		boolean ret = super.insertBefore(child, insertBefore);
		if ((child instanceof TableColumnField) && ret) {
			updateHeaders();
		}
		return ret;
	}
	
	@Override
	public boolean appendChild(Component child) {
		boolean ret = super.appendChild(child);
		if (ret && (child instanceof TableColumnField)) {
			updateHeaders();
		}
		return ret;
	}
	
	
	private void loadForm() throws Exception {
		if (StringUtils.isNullOrEmpty(formName)) {
			return;
		}
		Form f = ConfigLoader.getInstance().getForms().queryForId(formName);
		if (f == null) {
			return;
		}
		// we can not create resolver here, because it may cause infinity loop, so init it later
		form = f;
	}
	
	protected void updateHeaders() {
		if (_items == null)
			return;
		lstData.getListhead().getChildren().clear();
		for(TableColumnField column : _items) {
			Listheader header = new Listheader();
			header.setSort("auto");
			header.setLabel(column.getLabel());
			lstData.getListhead().appendChild(header);
		}
	}
	
	public void onCreate(Map<?,?> args) throws Exception {
		
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Component> List<T> getChildren() {
		return (List<T>) new Children();
	}
	
	private void init() {
		_items = new AbstractSequentialList<TableColumnField>() {
			public ListIterator<TableColumnField> listIterator(int index) {
				return new ItemIter(index);
			}
			
			public boolean add(TableColumnField field) {
				if (super.add(field)) {
					updateHeaders();
					return true;
				}
				return false;
			}
			
			public void add(int index, TableColumnField element) {
				super.add(index, element);
				updateHeaders();
			}
			
			public boolean addAll(Collection<? extends TableColumnField> c) {
				boolean ret = super.addAll(c);
				updateHeaders();
				return ret;
			}
			
			public boolean addAll(int index, Collection<? extends TableColumnField> c) {
				boolean ret = super.addAll(index, c);
				updateHeaders();
				return ret;
			}

			public TableColumnField get(int j) {
				final Component o = TableField.this.getChildren().get(j + _hdcnt);
				if (o instanceof TableColumnField)
					return (TableColumnField)o;
				throw new IndexOutOfBoundsException("Wrong index: " + j);
			}

			public int size() {
				int sz = getChildren().size() - _hdcnt;
				return sz;
			}

			/**
			 * override for update headers
			 *
			 * @since 3.5.1
			 */
			protected void removeRange(int fromIndex, int toIndex) {
				ListIterator<TableColumnField> it = listIterator(toIndex);
				for (int n = toIndex - fromIndex; --n >= 0 && it.hasPrevious();) {
					it.previous();
					it.remove();
				}
				updateHeaders();
			}
			
			
			/**
			 * Override to remove unnecessary Listitem re-indexing (when ROD is on, clear() is called frequently). 
			 */
			public void clear() {
				super.clear();
				updateHeaders();
			}
		};
	}
	
	private class ItemIter implements ListIterator<TableColumnField>, java.io.Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 705355367730620597L;
		
		private ListIterator<TableColumnField> _it;
		private int _j;
		private boolean _bNxt;

		private ItemIter(int index) {
			_j = index;
		}

		public void add(TableColumnField o) {
			prepare();
			_it.add(o);
			++_j;
		}

		public boolean hasNext() {
			return _j < _items.size();
		}

		public boolean hasPrevious() {
			return _j > 0;
		}

		public TableColumnField next() {
			if (!hasNext()) //use _items.size() to control if reach listfoot
				throw new NoSuchElementException();

			prepare();
			final TableColumnField o = _it.next();
			++_j;
			_bNxt = true;
			return o;
		}

		public TableColumnField previous() {
			if (!hasPrevious()) //use _j >= 0 to control if reach listhead
				throw new NoSuchElementException();

			prepare();
			final TableColumnField o = _it.previous();
			--_j;
			_bNxt = false;
			return o;
		}

		public int nextIndex() {
			return _j;
		}

		public int previousIndex() {
			return _j - 1;
		}

		public void remove() {
			if (_it == null)
				throw new IllegalStateException();
			_it.remove();
			if (_bNxt)
				--_j;
			updateHeaders();
		}

		public void set(TableColumnField o) {
			if (_it == null)
				throw new IllegalStateException();
			_it.set(o);
			updateHeaders();
		}

		private void prepare() {
			if (_it == null)
				_it = cast(getChildren().listIterator(_j + _hdcnt));
		}
	}
	
	protected class Children extends AbstractComponent.Children {
		protected void removeRange(int fromIndex, int toIndex) {
			ListIterator<Component> it = listIterator(toIndex);
			for (int n = toIndex - fromIndex; --n >= 0 && it.hasPrevious();) {
				it.previous();
				it.remove();
			}
		}
	};

}
