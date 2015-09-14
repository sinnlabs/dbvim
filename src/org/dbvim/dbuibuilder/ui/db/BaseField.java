/**
 * 
 */
package org.dbvim.dbuibuilder.ui.db;

import org.dbvim.dbuibuilder.db.Value;
import org.dbvim.dbuibuilder.db.model.DBField;
import org.dbvim.dbuibuilder.db.model.IDBField;
import org.dbvim.dbuibuilder.ui.IField;
import org.dbvim.dbuibuilder.zk.model.CurrentForm;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.event.KeyEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Idspace;
import org.zkoss.zul.Label;
import org.zkoss.zul.Space;
import org.zkoss.zul.impl.InputElement;

/**
 * Class implements basic db field logic
 * @author peter.liverovsky
 *
 */
public class BaseField<T, E extends InputElement> extends Idspace implements IField<T>, IDBField {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8390813627874916864L;
	
	protected DBField dbField;
	
	protected String map;
	
	@Wire
	protected Label label;
	
	@Wire
	protected E value;
	
	@Wire
	protected Space space;
	
	protected boolean isChildable = true;
	
	private boolean readOnly;
	

	protected BaseField(String zulUrl, DBField field) {
		isChildable = true;
		dbField = field;
		
		/* Create the ui */
		Executions.createComponents(zulUrl, this, null);
		Selectors.wireComponents(this, this, false);
		// lock the component to avoid to create new child elements.
		isChildable = false;
		readOnly = value.isReadonly();
		final BaseField<T, E> t = this;
		
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
	}
	
	/* (non-Javadoc)
	 * @see com.asd.dbuibuilder.db.model.IDBField#getName()
	 */
	@Override
	public String getName() {
		return dbField.getName();
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
	 * @see org.dbvim.dbuibuilder.ui.IField#setDBField(com.asd.dbuibuilder.db.model.DBField)
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
	 * @see org.dbvim.dbuibuilder.ui.IField#getMapping()
	 */
	@Override
	public String getMapping() {
		return map;
	}

	/* (non-Javadoc)
	 * @see org.dbvim.dbuibuilder.ui.IField#setMapping(java.lang.String)
	 */
	@Override
	public void setMapping(String map) {
		this.map = map;
		dbField = CurrentForm.getInstance().getDBFieldByMapping(map);
	}

	/* (non-Javadoc)
	 * @see org.dbvim.dbuibuilder.ui.IField#setDBValue(com.asd.dbuibuilder.db.Value)
	 */
	@Override
	public void setDBValue(Value<T> v) {
		value.setRawValue(v.getValue());
	}

	/* (non-Javadoc)
	 * @see org.dbvim.dbuibuilder.ui.IField#getDBValue()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public Value<T> getDBValue() {
		return new Value<T>((T)value.getRawValue(), dbField);
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
}
