/**
 * 
 */
package org.sinnlabs.dbvim.ui.db;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.sinnlabs.dbvim.db.Value;
import org.sinnlabs.dbvim.db.model.DBField;
import org.sinnlabs.dbvim.db.model.IDBField;
import org.sinnlabs.dbvim.form.FormFieldResolver;
import org.sinnlabs.dbvim.menu.MenuItem;
import org.sinnlabs.dbvim.menu.MenuResolver;
import org.sinnlabs.dbvim.menu.MenuResolverFactory;
import org.sinnlabs.dbvim.ui.IField;
import org.sinnlabs.dbvim.ui.events.MenuSelectEvent;
import org.sinnlabs.dbvim.ui.events.VimEvents;
import org.sinnlabs.dbvim.zk.model.IFormComposer;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.CreateEvent;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Idspace;
import org.zkoss.zul.Label;
import org.zkoss.zul.Menupopup;
import org.zkoss.zul.Space;
import org.zkoss.zul.impl.InputElement;

/**
 * Class implements basic db field logic
 * @author peter.liverovsky
 *
 */
public abstract class BaseField<T, E extends InputElement> extends Idspace implements IField<T>, IDBField {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8390813627874916864L;
	
	protected DBField dbField;
	
	protected String map;
	
	protected String formName;
	
	protected String menu;
	
	@Wire
	protected Label label;
	
	@Wire
	protected E value;
	
	@Wire
	protected Space space;
	
	@Wire
	protected Button btnMenu;
	
	protected IFormComposer composer;
	
	protected boolean isChildable = true;
	
	protected Menupopup popup;
	
	protected MenuResolver menuResolver = null;
	
	private boolean readOnly;
	
	private boolean displayOnly = false;
	

	protected BaseField(String zulUrl, DBField field) {
		super();
		isChildable = true;
		dbField = field;
		
		// initialize component after all attributes are loaded
		addEventListener(Events.ON_CREATE, new EventListener<CreateEvent>() {

			@Override
			public void onEvent(CreateEvent e) throws Exception {
				onCreate(e.getArg());
			}

		});

		/* Create the ui */
		Executions.createComponents(zulUrl, this, null);
		Selectors.wireComponents(this, this, false);
		// lock the component to avoid to create new child elements.
		isChildable = false;
		readOnly = value.isReadonly();
		final BaseField<T, E> t = this;
		
		/* init event listeners */
		value.addEventListener(Events.ON_CHANGE, new EventListener<InputEvent>() {

			@Override
			public void onEvent(InputEvent e) throws Exception {
				InputEvent ie = new InputEvent(e.getName(), t, e.getValue(), e.getPreviousValue());
				Events.postEvent(ie);
			}
			
		});
		
		value.addEventListener(Events.ON_FOCUS, new EventListener<Event>() {

			@Override
			public void onEvent(Event e) throws Exception {
				Event ne = new Event(e.getName(), t, e.getData());
				Events.postEvent(ne);
			}
			
		});
		
		value.addEventListener(Events.ON_BLUR, new EventListener<Event>() {

			@Override
			public void onEvent(Event e) throws Exception {
				Event ne = new Event(e.getName(), t, e.getData());
				Events.postEvent(ne);
			}
			
		});
		
		value.addEventListener(Events.ON_OK, new EventListener<KeyEvent>() {

			@Override
			public void onEvent(KeyEvent e) throws Exception {
				KeyEvent ne = new KeyEvent(e.getName(), t, e.getKeyCode(), 
						e.isCtrlKey(), e.isShiftKey(), e.isAltKey(), e.getReference());
				Events.postEvent(ne);
			}
			
		});
		
		value.addEventListener(Events.ON_CTRL_KEY, new EventListener<KeyEvent>() {

			@Override
			public void onEvent(KeyEvent e) throws Exception {
				KeyEvent ne = new KeyEvent(e.getName(), t, e.getKeyCode(), 
						e.isCtrlKey(), e.isShiftKey(), e.isAltKey(), e.getReference());
				Events.postEvent(ne);
			}
			
		});
		
		value.addEventListener(Events.ON_DROP, new EventListener<DropEvent>() {

			@Override
			public void onEvent(DropEvent e) throws Exception {
				DropEvent ne = new DropEvent(e.getName(), t, e.getDragged(), 
						e.getX(), e.getY(), e.getPageX(), e.getPageY(), e.getKeys());
				Events.postEvent(ne);
			}
			
		});
		
		if (btnMenu != null) {
			btnMenu.addEventListener(Events.ON_CLICK, new EventListener<MouseEvent>() {

				@Override
				public void onEvent(MouseEvent arg0) throws Exception {
					if (popup != null)
						popup.open(btnMenu);
				}
				
			});
		}
	}
	
	/**
	 * Initialize menu items
	 * @throws Exception
	 */
	private void initMenu() throws Exception {
		if (menuResolver != null) {
			popup = new Menupopup();
			popup.setStyle("overflow: auto; max-height: 100vh;");
			
			for(MenuItem i : menuResolver.getItems()) {
				// Add items to the popup menu
				FieldMenuItem item = new FieldMenuItem(i);
				item.setLabel(i.getLabel().toString());
				
				/** Add item event listener **/
				item.addEventListener(Events.ON_CLICK, new EventListener<MouseEvent>() {

					@Override
					public void onEvent(MouseEvent evnt) throws Exception {
						if (evnt.getTarget() != null) {
							FieldMenuItem item = (FieldMenuItem) evnt.getTarget();
							// if user defined the event listener onMenuSelect
							// then we do not take default action
							// User must implement logic manually
							if (Events.isListened(BaseField.this, VimEvents.ON_MENUSELECTED, false)) {
								Event e = new MenuSelectEvent(
										VimEvents.ON_MENUSELECTED, BaseField.this, item.getItem());
								Events.postEvent(e);
							} else {
								// if event listener is not defined
								// then we just set the field value
								
								// if value is a String
								if (item.getItem().getValue() instanceof String)
									BaseField.this.fromString((String) item.getItem().getValue());
								else
									BaseField.this.setDBValue(fromObject(item.getItem().getValue()));
							}
						}
					}
					
				});
				popup.appendChild(item);
			}
			// add popup to the field
			isChildable = true;
			this.appendChild(popup);
			isChildable = false;
		}
	}
	
	/* (non-Javadoc)
	 * @see com.asd.dbuibuilder.db.model.IDBField#getName()
	 */
	@Override
	public String getName() {
		return dbField.getName();
	}
	
	@Override
	public String getTableName() {
		return dbField.getTableName();
	}
	
	@Override
	public String getCatalogName() {
		return dbField.getCatalogName();
	}
	
	@Override
	public String getFullName() {
		return dbField.getFullName();
	}

	/* (non-Javadoc)
	 * @see com.asd.dbuibuilder.db.model.IDBField#getDBTypeName()
	 */
	@Override
	public String getDBTypeName() {
		return dbField.getDBTypeName();
	}

	/* (non-Javadoc)
	 * @see com.asd.dbuibuilder.db.model.IDBField#getDBType()
	 */
	@Override
	public int getDBType() {
		return dbField.getDBType();
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.ui.IField#setDBField(com.asd.dbuibuilder.db.model.DBField)
	 */
	@Override
	public void setDBField(DBField field) {
		dbField = field;
		if (field != null) {
			label.setValue(field.getName());
			if (field.isGenerated()) {
				value.setReadonly(true);
			}
		}
		map = field.getName();
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.ui.IField#getMapping()
	 */
	@Override
	public String getMapping() {
		return map;
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.ui.IField#setMapping(java.lang.String)
	 */
	@Override
	public void setMapping(String map) {
		this.map = map;
	}
	
	@Override
	public String getForm() {
		return formName;
	}
	
	@Override
	public void setForm(String form) {
		formName = form;
	}
	
	public String getMenu() {
		return menu;
	}
	
	public void setMenu(String menu) throws Exception {
		this.menu = menu;
		if (btnMenu != null && StringUtils.isNotEmpty(menu)) {
			btnMenu.setVisible(true);
		}
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.ui.IField#setDBValue(com.asd.dbuibuilder.db.Value)
	 */
	@Override
	public void setDBValue(Value<T> v) {
		value.setRawValue(v.getValue());
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.ui.IField#getDBValue()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Value<T> getDBValue() {
		return new Value<T>((T)value.getRawValue(), dbField);
	}
	
	@Override
	public void setValue(T val) {
		value.setRawValue(val);
	}
	
	@Override
	public void setValueFromString(String v) {
		Value<T> val = fromString(v);
		setDBValue(val);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public T getValue() {
		return (T)value.getRawValue();
	}

	public String getLabel() {
		return label.getValue();
	}
	
	public void setLabel(String l) {
		label.setValue(l);
	}
	
	public boolean isReadonly() {
		return value.isReadonly();
	}
	
	public void setReadonly(boolean val) {
		value.setReadonly(val);
		readOnly = val;
	}
	
	public String getSpace() {
		return space.getSpacing();
	}
	
	public void setSpace(String sp) {
		space.setSpacing(sp);
	}
	
	public void setHflex(String flex) {
		value.setHflex(flex);
	}
	
	public String getHflex() {
		return value.getHflex();
	}

	@Override
	public boolean isGenerated() {
		return dbField.isGenerated();
	}

	@Override
	public boolean isNullable() {
		return dbField.isNullable();
	}

	@Override
	public void setErrorMessage(String err) {
		value.setErrorMessage(err);
	}

	@Override
	public void clearErrorMessage() {
		value.clearErrorMessage();
	}
	
	@Override
	protected boolean isChildable() {
		return isChildable;
	}

	@Override
	public void setFieldMode(int mode) {
		if (mode == IField.MODE_SEARCH) {
			value.setReadonly(false);
		} else if (mode == IField.MODE_MODIFY) {
			value.setReadonly(readOnly);
		}
	}
	
	@Override
	public DBField getDBField() {
		return dbField;
	}
	
	@Override
	public boolean isDisplayOnly() {
		return displayOnly;
	}
	
	@Override
	public void setDisplayOnly(boolean val) {
		displayOnly = val;
	}
	
	/**
	 * @return Returns menu resolver or null if menu is not set
	 */
	public MenuResolver getMenuResolver() {
		return menuResolver;
	}
	
	@Override
	public void onCreate(Map<?,?> args) throws Exception {
		if (args != null) {
			if (!displayOnly) {
				FormFieldResolver f = (FormFieldResolver) args.get("resolver");
				if (f != null) {
					dbField = f.getFieldByMapping(formName, map);
				}
			}
			Object c = args.get("composer");
			if (c!= null) {
				composer = (IFormComposer) c;
				if (StringUtils.isNotBlank(menu)) {
					menuResolver = MenuResolverFactory.getMenuResolver(menu, composer);
					if (btnMenu != null) {
						btnMenu.setVisible(true);
					}
					initMenu();
					if (menuResolver == null) {
						throw new IllegalStateException("Field menu does not exists: " + menu);
					}
				}
			}
		}
	}
}
