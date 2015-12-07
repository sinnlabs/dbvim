/**
 * 
 */
package org.sinnlabs.dbvim.ui.db;

import org.sinnlabs.dbvim.db.Value;
import org.sinnlabs.dbvim.db.model.DBField;
import org.zkoss.zul.Textbox;

/**
 * A CharacterField.
 * Used to represent character data.
 * @author peter.liverovsky
 *
 */
public class CharacterField extends BaseField<String, Textbox> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2682736027373652629L;
	
	
	public CharacterField(DBField dbfield) {
		super("/components/characterfield.zul", dbfield);
		
	}
	
	public CharacterField() {
		this(null);
	}
	
	/**
	 * Sets the rows.
	 * @param rows
	 */
	public void setRows(int rows) {
		value.setRows(rows);
	}
	
	/**
	 * Returns the rows.
	 * @return
	 */
	public int getRows() {
		return value.getRows();
	}
	
	/**
	 * Sets whether it is multiline.
	 * @param multiline
	 */
	public void setMultiline(boolean multiline) {
		value.setMultiline(multiline);
	}
	
	/**
	 * Returns whether it is multiline.
	 * @return
	 */
	public boolean isMultiline() {
		return value.isMultiline();
	}
	
	/**
	 * Sets whether TAB is allowed.
	 * @param tabbable
	 */
	public void setTabbable(boolean tabbable) {
		value.setTabbable(tabbable);
	}
	
	/**
	 * Returns whether TAB is allowed.
	 * @return
	 */
	public boolean isTabbable() {
		return value.isTabbable();
	}
	
	@Override
	public Value<String> getDBValue() {
		if (value.getValue().isEmpty())
			return new Value<String>(null, dbField);
		return new Value<String>(value.getValue(), dbField);
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.ui.IField#fromString(java.lang.String)
	 */
	@Override
	public Value<String> fromString(String string) {
		return new Value<String>(string, dbField);
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.ui.IField#fromObject(java.lang.Object)
	 */
	@Override
	public Value<String> fromObject(Object val) {
		return new Value<String>((String) val, dbField);
	}
}