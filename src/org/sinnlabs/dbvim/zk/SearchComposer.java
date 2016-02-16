/**
 * 
 */
package org.sinnlabs.dbvim.zk;

import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sinnlabs.dbvim.db.Database;
import org.sinnlabs.dbvim.db.DatabaseFactory;
import org.sinnlabs.dbvim.db.Entry;
import org.sinnlabs.dbvim.db.Value;
import org.sinnlabs.dbvim.db.exceptions.DatabaseOperationException;
import org.sinnlabs.dbvim.db.model.IDBField;
import org.sinnlabs.dbvim.evaluator.exceptions.ParseException;
import org.sinnlabs.dbvim.form.FormFieldResolver;
import org.sinnlabs.dbvim.form.FormFieldResolverFactory;
import org.sinnlabs.dbvim.model.Form;
import org.sinnlabs.dbvim.model.ResultColumn;
import org.sinnlabs.dbvim.script.ScriptApi;
import org.sinnlabs.dbvim.ui.IField;
import org.sinnlabs.dbvim.ui.annotations.EventType;
import org.sinnlabs.dbvim.ui.db.ConditionFieldMenuitem;
import org.sinnlabs.dbvim.ui.events.VimEvents;
import org.sinnlabs.dbvim.zk.model.FormEventProcessor;
import org.sinnlabs.dbvim.zk.model.IFormComposer;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Idspace;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listfooter;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.North;
import org.zkoss.zul.South;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.impl.InputElement;

/**
 * Class implements zk composer
 * 
 * @author peter.liverovsky
 *
 */
// @VariableResolver(org.zkoss.zkplus.spring.DelegatingVariableResolver.class)
public class SearchComposer extends SelectorComposer<Component> implements IFormComposer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8674446600172735254L;
	
	protected static final int MODE_SEARCH = 1;
	
	protected static final int MODE_RESULT = 2;
	
	protected static final int MODE_CREATE = 3;
	
	protected static final int MODE_CHANGE = 4;

	@Wire("#border #searchResults")
	North searchResults;

	@Wire("#border #center")
	Idspace detailsView;
	
	@Wire("#border #btnSearch")
	Button btnSearch;
	
	@Wire("#lstResults")
	Listbox results;
	
	@Wire("#btnNewSearch")
	Toolbarbutton btnNewSearch;
	
	@Wire("#btnSave")
	Toolbarbutton btnSave;
	
	@Wire("#btnChangeAll")
	Toolbarbutton btnChange;
	
	@Wire("#btnCopyToNew")
	Toolbarbutton btnCopy;
	
	@Wire("#btnDelete")
	Toolbarbutton btnDelete;
	
	@Wire("#btnCreate")
	Toolbarbutton btnCreate;
	
	@Wire("#btnNewEntry")
	Toolbarbutton btnNewQuery;
	
	@Wire("#btnAdditionalSearch")
	Toolbarbutton btnAdditionalSearch;
	
	@Wire("#lstFooterTotal")
	Listfooter lstFooter;
	
	@Wire("#divSearch")
	Hlayout divSearch;
	
	@Wire("#divNewEntry")
	Hlayout divNewEntry;
	
	@Wire("#divModify")
	Hlayout divModify;
	
	@Wire("#divChange")
	Hlayout divChange;
	
	@Wire("#south")
	South south;
	
	@Wire("#txtAdditionalSearch")
	Textbox txtAdditionalSearch;
	
	@Wire("#btnFields")
	Button btnFields;

	/**
	 * Current form
	 */
	Form form;
	
	/**
	 * Current form resolver
	 */
	FormFieldResolver resolver;
	
	/**
	 * Form events processor
	 */
	FormEventProcessor eventProcessor;
	
	/**
	 * Current entry
	 */
	Entry currentEntry;
	
	/**
	 * List of all form fields
	 */
	List<Component> fieldList;
	
	/**
	 * Contains all form fields
	 */
	List<IField<?>> fields;
	
	/**
	 * List of all read only fields
	 */
	List<Component> readonlyFields;
	
	/**
	 * Database object for the current form
	 */
	Database db;
	
	/**
	 * Api for the scripting
	 */
	ScriptApi api;
	
	/**
	 * Menu contains all form fields
	 */
	protected Menupopup fieldsPopup;
	
	List<Value<?>> lastSearch;
	
	private LastSearch search;
	
	private int currentViewMode;

	private boolean isAdditional = false;
	
	public List<IField<?>> getFields() { return fields; }
	
	public ScriptApi getApi() { return api; }
	
	private class LastSearch {
		public List<Value<?>> values;
		public String additional;
	}
	
	public ComponentInfo doBeforeCompose(Page page, Component parent, ComponentInfo compInfo) {
		// We must do initialization before ui will be created
		// initialize all for related objects
		search = new LastSearch();
		form = (Form) Executions.getCurrent().getArg().get("form");
		
		try {
			resolver = FormFieldResolverFactory.getResolver(form);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// init field lists
		fieldList = new ArrayList<Component>();
		fields = new ArrayList<IField<?>>();
		eventProcessor = new FormEventProcessor();
		
		try {
			db = DatabaseFactory.createInstance(form, resolver);
		} catch (ClassNotFoundException | DatabaseOperationException
				| SQLException e) {
			e.printStackTrace();
		}
		// init script api object
		api = new ScriptApi(this);
		
		return super.doBeforeCompose(page, parent, compInfo);
	}
	
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		// create form ui
		loadForm();
		
		currentEntry = null;
		// build search result headers
		results.getListhead().setSizable(true);
		results.setMultiple(true);
		if (form.getResultList() != null) {
			for(ResultColumn column : form.getResultList()) {
				Listheader header = new Listheader();
				header.setSort("auto");
				header.setLabel(column.label);
				results.getListhead().appendChild(header);
			}
		}
		
		/** Invoke form loaded event **/
		try {
			eventProcessor.Invoke(EventType.FORM_LOADED, (Object[]) null);
		} catch (Exception e) {
			Messagebox.show("Unable to raise onFormLoaded event.", "error", Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
		}
		
		setMode(MODE_SEARCH);
		
		/** init fields popup menu **/
		initFieldsMenu();
		
		/** process request parameters **/
		@SuppressWarnings("unchecked")
		Map<String, String[]> params = 
				(Map<String, String[]>) Executions.getCurrent().getArg().get("params");
		if (params != null) {
			if (params.containsKey("query")) {
				String query = params.get("query")[0];
				isAdditional = true;
				txtAdditionalSearch.setText(query);
				search(new ArrayList<Value<?>>());
			} else if (params.containsKey("mode")) {
				String formMode = params.get("mode")[0];
				if (formMode.equals("create")) {
					setMode(MODE_CREATE);
				} else if (formMode.equals("search")) {
					setMode(MODE_SEARCH);
				}
				/** populate fields **/
				for (IField<?> f : fields) {
					String s = "'" + f.getId() + "'";
					if (params.containsKey(s)) {
						f.setValueFromString((params.get(s)[0]));
					} else if (params.containsKey(f.getId())) {
						f.setValueFromString(params.get(f.getId())[0]);
					}
				}
			}
		}
	}
	
	/**
	 * Initialize field select menu
	 */
	private void initFieldsMenu() {
		fieldsPopup = new Menupopup();
		fieldsPopup.setStyle("overflow: auto; max-height: 100vh;");
		detailsView.appendChild(fieldsPopup);
		for (IField<?> c : fields) {
			final ConditionFieldMenuitem i = new ConditionFieldMenuitem(c);
			i.setLabel(c.getId() + " ("  + c.getLabel() + ")");
			
			/** Add item event listener **/
			i.addEventListener(Events.ON_CLICK, new EventListener<MouseEvent>() {

				@Override
				public void onEvent(MouseEvent arg0) throws Exception {
					txtAdditionalSearch.setText(txtAdditionalSearch.getText() + 
							"'" + i.getField().getId() + "'");
				}
				
			});
			
			/** add item **/
			fieldsPopup.appendChild(i);
		}
	}
	
	@Listen("onClick = #btnFields")
	public void btnFields_onClick() {
		if (fieldsPopup != null) {
			fieldsPopup.open(btnFields);
		}
	}
	
	@Listen("onClick = #btnEq")
	public void btnEq_onClick() {
		txtAdditionalSearch.setText(txtAdditionalSearch.getText() + " = ");
	}
	
	@Listen("onClick = #btnNotEq")
	public void btnNotEq_onClick() {
		txtAdditionalSearch.setText(txtAdditionalSearch.getText() + " != ");
	}
	
	@Listen("onClick = #btnLt")
	public void btnLt_onClick() {
		txtAdditionalSearch.setText(txtAdditionalSearch.getText() + " < ");
	}
	
	@Listen("onClick = #btnGt")
	public void btnGt_onClick() {
		txtAdditionalSearch.setText(txtAdditionalSearch.getText() + " > ");
	}
	
	@Listen("onClick = #btnLtEq")
	public void btnLtEq_onClick() {
		txtAdditionalSearch.setText(txtAdditionalSearch.getText() + " <= ");
	}
	
	@Listen("onClick = #btnGtEq")
	public void btnGtEq_onClick() {
		txtAdditionalSearch.setText(txtAdditionalSearch.getText() + " >= ");
	}
	
	@Listen("onClick = #btnLIKE")
	public void btnLIKE_onClick() {
		txtAdditionalSearch.setText(txtAdditionalSearch.getText() + " LIKE ");
	}
	
	@Listen("onClick = #btnAND")
	public void btnAND_onClick() {
		txtAdditionalSearch.setText(txtAdditionalSearch.getText() + " AND ");
	}
	
	@Listen("onClick = #btnOR")
	public void btnOR_onClick() {
		txtAdditionalSearch.setText(txtAdditionalSearch.getText() + " OR ");
	}
	
	@Listen("onClick = #btnNOT")
	public void btnNOT_onClick() {
		txtAdditionalSearch.setText(txtAdditionalSearch.getText() + " NOT ");
	}
	
	@Listen("onClick = #btnSearch")
	public void btnSearch_onClick() {
		List<Value<?>> userValues = getUserValues();
		lastSearch = userValues;
		search(lastSearch);
	}
	
	@Listen("onSelect = #lstResults")
	public void lstResults_onSelect() {
		Entry e = results.getSelectedItem().getValue();
		setMode(MODE_RESULT);
		try {
			currentEntry = db.readEntry(e);
		} catch (DatabaseOperationException ex) {
			Messagebox.show("DB Operation error: " + ex.getMessage(), "ERROR",
					Messagebox.OK, Messagebox.ERROR);
			ex.printStackTrace();
			return;
		}
		populateFields();
		raiseOnEntryLoadedEvent();
	}
	
	@Listen("onClick = #btnNewSearch")
	public void btnNewSearch_onClick() {
		setMode(MODE_SEARCH);
		clearAllFields(detailsView);
	}
	
	@Listen("onClick = #btnAdditionalSearch")
	public void btnAdditionalSearch_onClick() {
		setAdditionalSearch(!isAdditional);
	}
	
	@Listen("onClick = #btnNewEntry")
	public void btnNewEntry_onClick() {
		setMode(MODE_CREATE);
		clearAllFields(detailsView);
	}
	
	@Listen("onClick = #btnChangeAll")
	public void btnChangeAll_onClick() {
		setMode(MODE_CHANGE);
		clearAllFields(detailsView);
	}
	
	@Listen("onClick = #btnSave")
	public void btnSave_onClick() {
		if (currentEntry != null && currentViewMode == MODE_RESULT) {
			if (scanForNulls()) {
				return;
			}
			// get values
			List<Value<?>> values = new ArrayList<Value<?>>();
			for(Component c : fieldList) {
				if (!((IField<?>)c).isDisplayOnly())
					values.add(((IField<?>)c).getDBValue());
			}
			
			//update entry
			try {
				db.updateEntry(currentEntry, values);
			} catch (DatabaseOperationException e) {
				Messagebox.show("Unable to update entry.", "Update error.", 
						Messagebox.OK, Messagebox.ERROR);
				e.printStackTrace();
				return;
			}
			
			// mark as modified
			if (results.getSelectedItem() != null) {
				markItemAsChanged(results.getSelectedItem());
			}
		} else if (currentViewMode == MODE_CHANGE) {
			Messagebox.show("You are shure that you want to update " + this.results.getItemCount() +
					" entries.", 
					"Update entries", Messagebox.YES | Messagebox.NO, Messagebox.QUESTION, 
					new EventListener<Event>() {

						@Override
						public void onEvent(Event evnt) throws Exception {
							if (evnt.getName().equals(Messagebox.ON_YES)) {
								updateAllEntries();
							}
						}
				
			});
		}
	}
	
	private void markItemAsChanged(Listitem item) {
		for (Component c : item.getChildren()) {
			if (c instanceof Listcell) {
				((Listcell) c).setStyle("font-style: italic;");
			}
		}
	}
	
	@Listen("onClick = #btnCreate")
	public void btnCreate_onClick() {
		if (scanForNulls()) {
			return;
		}
		// get values
		Entry e = new Entry();
		
		for(Component c : fieldList) {
			IField<?> f = (IField<?>) c;
			if (f.getDBValue().getValue() != null && !f.isDisplayOnly())
				e.getValues().add(f.getDBValue());
		}
		// if no user values
		if (e.getValues().size() == 0)
			return;
		// create new entry
		try {
			db.insertEntry(e);
			clearAllFields(detailsView);
		} catch (DatabaseOperationException e1) {
			Messagebox.show("Unable to create entry");
			e1.printStackTrace();
		}
	}
	
	@Listen("onClick = #btnDelete")
	public void btnDelete_onClick() {
		if (currentEntry != null) {
			Messagebox.show("You are shure that you want to delete entries?", "Delete",
					Messagebox.YES|Messagebox.NO, Messagebox.QUESTION, new EventListener<Event>() {

						@Override
						public void onEvent(Event evnt) throws Exception {
							if (evnt.getName().equals(Messagebox.ON_YES)) {
								try {
									for(Listitem i : results.getSelectedItems()) {
										Entry e = i.getValue();
										db.deleteEntry(e);
									}
								} catch (DatabaseOperationException e) {
									Messagebox.show("Unable to delte entry.", "Error", 
											Messagebox.OK, Messagebox.ERROR);
									e.printStackTrace();
								}
								search(lastSearch);
							}
						}
				
			});
			
		}
	}
	
	/**
	 * Updates all fields from result list with value
	 * @throws ParseException
	 * @throws DatabaseOperationException
	 */
	private void updateAllEntries() throws ParseException, DatabaseOperationException {
		if (search == null)
			return;
		
		// get the user values
		List<Value<?>> values = getUserValues();
		
		// update the records
		if (search.values == null && search.additional != null) {
			db.update(values, search.additional, null);
		} else if (search.values != null && search.additional == null) {
			db.update(search.values, values);
		} else if (search.values == null && search.additional == null) {
			db.updateAll(values);
		}
		
		// mark all result list items as changed
		for (Listitem i : results.getItems()) {
			markItemAsChanged(i);
		}
	}
	
	/**
	 * Perform search on the form
	 * @param values - User entered values, that can be used for filtering
	 */
	private void search(List<Value<?>> values) {
		List<Entry> entries = null;
		if (values == null)
			return;
		try {
			if (isAdditional) {
				List<IField<?>> fields = new ArrayList<IField<?>>();
				for(Component c : fieldList) {
					fields.add((IField<?>) c);
				}
				entries = db.query(null, txtAdditionalSearch.getText(), 0, null);
				search.values = null;
				search.additional = txtAdditionalSearch.getText();
			} else if (isAdditional == false && values.size() == 0) {
				entries = db.queryAll(null, 0);
				search.values = null;
				search.additional = null;
			} else {
				entries = db.query(null, values, 0);
				search.values = values;
				search.additional = null;
			}
		} catch(DatabaseOperationException e) {
			Messagebox.show("DB Operation error: " + e.getMessage(), "ERROR",
					Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
			return;
		} catch (WrongValueException e1) {
			Messagebox.show(e1.getMessage(), "ERROR",
					Messagebox.OK, Messagebox.ERROR);
			e1.printStackTrace();
		} catch (ParseException e1) {
			Messagebox.show(e1.getMessage(), "ERROR",
					Messagebox.OK, Messagebox.ERROR);
			e1.printStackTrace();
		}
		results.getItems().clear();
		if (entries == null || entries.size() == 0) {
			Messagebox.show("No etries found.");
			return;
		}
		for(Entry e : entries) {
			Listitem item = new Listitem();
			item.setValue(e);
			for (Value<?> v : e.getValues()) {
				Listcell cell = new Listcell();
				if (v.getValue() != null)
					cell.setLabel(v.getValue().toString());
				item.appendChild(cell);
			}
			results.getItems().add(item);
		}
		lstFooter.setLabel(results.getItemCount() + " entries");
		// select first item
		if (results.getItemCount() > 0) {
			results.setSelectedIndex(0);
			currentEntry = results.getSelectedItem().getValue();
			try {
				currentEntry = db.readEntry(currentEntry);
			} catch (DatabaseOperationException e) {
				Messagebox.show("DB Operation error: " + e.getMessage(), "ERROR",
						Messagebox.OK, Messagebox.ERROR);
				e.printStackTrace();
				return;
			}
			populateFields();
		}
		setMode(MODE_RESULT);
		raiseOnEntryLoadedEvent();
	}
	
	/**
	 * Checks that all not nullable fields has the assigned value.
	 * Shows notification to user if not nullable field set to null
	 * @return False if all not nullable fields has a value, otherwise True
	 */
	private boolean scanForNulls() {
		boolean ret = false;
		for(Component c : this.fieldList) {
			IField<?> f = (IField<?>) c;
			IDBField dbf = (IDBField) c;
			if (f.getDBValue().getValue() == null && !dbf.isNullable() && !dbf.isGenerated()) {
				f.setErrorMessage("Value can not be null.");
				ret = true;
			}
		}
		return ret;
	}
	
	/**
	 * Creates form ui
	 * @throws Exception
	 */
	private void loadForm() throws Exception {
		// get form view definition
		StringReader r = new StringReader(form.getView());
		// sets executions parameters
		HashMap<String, Object> args = new HashMap<String, Object>();
		args.put("resolver", resolver);
		args.put("composer", this);
		// create the ui
		Executions.createComponentsDirectly(r, null, detailsView, args);
		Selectors.wireVariables(detailsView, this, null);
		
		// Find all DB fields of the form and wire all events
		findAllDBFields(detailsView);
	}
	
	private void findAllDBFields(Component c) {
		if (c == null || c.getChildren() == null)
			return;
		for(Component child : c.getChildren()) {
			eventProcessor.addListeners(child);
			if (child instanceof IField) {
				fieldList.add(child);
				fields.add((IField<?>) child);
				// force field initialization
				HashMap<String, Object> args = new HashMap<String, Object>();
				args.put("resolver", resolver);
				args.put("composer", this);
				try {
					((IField<?>) child).onCreate(args);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			/** RECURSION **/
			findAllDBFields(child);
		}
	}
	
	private void clearAllFields(Component c) {
		if (c == null || c.getChildren() == null)
			return;
		for(Component child : c.getChildren()) {
			if (child instanceof InputElement) {
				((InputElement)child).setRawValue(null);
				((InputElement)child).clearErrorMessage();
			}
			/** RECURSION **/
			clearAllFields(child);
		}
	}
	
	/**
	 * Find all mapped field that has user values
	 * @return List of fields values
	 */
	private List<Value<?>> getUserValues() {
		List<Value<?>> list = new ArrayList<Value<?>>();
		for (Component c : fieldList) {
			IField<?> f = (IField<?>) c;
			
			// Skip all display only fields
			if (f.isDisplayOnly())
				continue;
			
			Value<?> v = f.getDBValue();
			// if value is set then add it to the condition
			if (v.getValue() != null) {
				list.add(v);
			}
		}
		return list;
	}
	
	/**
	 * Fill form fields with currentEntry values
	 */
	@SuppressWarnings("unchecked")
	private void populateFields() {
		// If currentEntry not set
		if (currentEntry == null)
			return;
		
		// read all fields
		for(Component c : fieldList) {
			IDBField dbField = (IDBField) c;
			IField<Object> field = (IField<Object>) c;
			for(Value<?> v : currentEntry.getValues()) {
				// find value for the field
				if (!field.isDisplayOnly() && 
						dbField.getFullName().equals(v.getDBField().getFullName())) {
					// sets the value
					field.setDBValue((Value<Object>) v);
				}
			}
		} // for
	} // populateFields
	
	/**
	 * Changes form view mode (create, modify, etc)
	 * @param mode Form view mode
	 */
	private void setMode(int mode) {
		currentViewMode = mode;
		if (mode == MODE_SEARCH) {
			searchResults.setVisible(false);
			btnSearch.setVisible(true);
			btnSave.setVisible(false);
			btnCreate.setVisible(false);
			btnChange.setDisabled(true);
			btnCopy.setDisabled(true);
			btnDelete.setDisabled(true);
			btnAdditionalSearch.setDisabled(false);
			south.setVisible(false);
			divSearch.setVisible(true);
			divNewEntry.setVisible(false);
			divModify.setVisible(false);
			divChange.setVisible(false);
			setFieldsMode(IField.MODE_SEARCH);
			setAdditionalSearch(false);
			try {
				eventProcessor.Invoke(EventType.CHANGE_FORM_MODE, new Object[] {IField.MODE_SEARCH});
			} catch (Exception e) {
				Messagebox.show("Unable to change field mode.", "error", Messagebox.OK, Messagebox.ERROR);
				e.printStackTrace();
			}
		}
		if (mode == MODE_RESULT) {
			searchResults.setVisible(true);
			btnSearch.setVisible(false);
			btnSave.setVisible(true);
			btnCreate.setVisible(false);
			btnChange.setDisabled(false);
			btnCopy.setDisabled(false);
			btnDelete.setDisabled(false);
			btnAdditionalSearch.setDisabled(true);
			south.setVisible(false);
			divSearch.setVisible(false);
			divNewEntry.setVisible(false);
			divModify.setVisible(true);
			divChange.setVisible(false);
			setFieldsMode(IField.MODE_MODIFY);
			try {
				eventProcessor.Invoke(EventType.CHANGE_FORM_MODE, new Object[] {IField.MODE_MODIFY});
			} catch (Exception e) {
				Messagebox.show("Unable to change field mode.", "error", Messagebox.OK, Messagebox.ERROR);
				e.printStackTrace();
			}
		}
		if (mode == MODE_CREATE) {
			searchResults.setVisible(false);
			btnSearch.setVisible(false);
			btnSave.setVisible(false);
			btnCreate.setVisible(true);
			btnChange.setDisabled(true);
			btnCopy.setDisabled(true);
			btnDelete.setDisabled(true);
			btnAdditionalSearch.setDisabled(true);
			south.setVisible(false);
			divSearch.setVisible(false);
			divNewEntry.setVisible(true);
			divModify.setVisible(false);
			divChange.setVisible(false);
			if (form.isJoin())
				btnCreate.setDisabled(true);
			else
				btnCreate.setDisabled(false);
			setFieldsMode(IField.MODE_MODIFY);
			try {
				eventProcessor.Invoke(EventType.CHANGE_FORM_MODE, new Object[] {IField.MODE_MODIFY});
			} catch (Exception e) {
				Messagebox.show("Unable to change field mode.", "error", Messagebox.OK, Messagebox.ERROR);
				e.printStackTrace();
			}
		}
		if (mode == MODE_CHANGE) {
			searchResults.setVisible(true);
			btnSearch.setVisible(false);
			btnSave.setVisible(true);
			btnCreate.setVisible(false);
			btnChange.setDisabled(false);
			btnCopy.setDisabled(false);
			btnDelete.setDisabled(false);
			btnAdditionalSearch.setDisabled(true);
			south.setVisible(false);
			divSearch.setVisible(false);
			divNewEntry.setVisible(false);
			divModify.setVisible(false);
			divChange.setVisible(true);
			setFieldsMode(IField.MODE_MODIFY);
			try {
				eventProcessor.Invoke(EventType.CHANGE_FORM_MODE, new Object[] {IField.MODE_MODIFY});
			} catch (Exception e) {
				Messagebox.show("Unable to change field mode.", "error", Messagebox.OK, Messagebox.ERROR);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Shows additional search bar
	 * @param b True - show bar, otherwise false
	 */
	private void setAdditionalSearch(boolean b) {
		south.setVisible(b);
		isAdditional  = b;
	}
	
	/**
	 * Sets view mode to all form fields
	 * @param mode
	 */
	private void setFieldsMode(int mode) {
		for(IField<?> f : fields){
			f.setFieldMode(mode);
		}
	}
	
	/**
	 * Raise the onEntryLoaded event
	 */
	private void raiseOnEntryLoadedEvent() {
		/** Invoke entry loaded event **/
		try {
			eventProcessor.Invoke(EventType.ENTRY_LOADED, new Object[] {currentEntry});
		} catch (Exception e1) {
			Messagebox.show("Unable to raise onEntryLoaded event.", "error", Messagebox.OK, Messagebox.ERROR);
			e1.printStackTrace();
		}
		// raise onEntryLoaded event for the fields
		for (Component f : fieldList) {
			Event e = new Event(VimEvents.ON_ENTRYLOADED, f);
			Events.postEvent(e);
		}
	}

}