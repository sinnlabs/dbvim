/**
 * 
 */
package org.dbvim.dbuibuilder.ui.db;

import org.dbvim.dbuibuilder.db.Value;
import org.dbvim.dbuibuilder.db.model.DBField;
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
}