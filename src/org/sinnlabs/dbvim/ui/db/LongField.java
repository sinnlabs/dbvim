/**
 * 
 */
package org.sinnlabs.dbvim.ui.db;

import org.sinnlabs.dbvim.db.Value;
import org.sinnlabs.dbvim.db.model.DBField;
import org.zkoss.zul.Longbox;

/**
 * @author peter.liverovsky
 *
 */
public class LongField extends BaseField<Long, Longbox> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7510213526283370164L;

	public LongField(DBField field) {
		super("/components/longfield.zul", field);
	}
	
	public LongField() {
		this(null);
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.ui.IField#fromString(java.lang.String)
	 */
	@Override
	public Value<Long> fromString(String string) {
		if (string == null) {
			return new Value<Long>(null, dbField);
		}
		return new Value<Long>(Long.valueOf(string), dbField);
	}
}