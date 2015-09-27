/**
 * 
 */
package org.sinnlabs.dbvim.ui.db;

import org.sinnlabs.dbvim.db.Value;
import org.sinnlabs.dbvim.db.model.DBField;
import org.zkoss.zul.Textbox;

/**
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
}