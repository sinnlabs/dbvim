/**
 * 
 */
package org.sinnlabs.dbvim.ui.db;

import org.sinnlabs.dbvim.db.Value;
import org.sinnlabs.dbvim.db.model.DBField;
import org.zkoss.zul.Doublebox;

/**
 * @author peter.liverovsky
 *
 */
public class DoubleField extends BaseField<Double, Doublebox> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5310013573116983009L;

	public DoubleField() {
		this(null);
	}
	
	public DoubleField(DBField field) {
		super("/components/doublefield.zul", field);
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.ui.IField#fromString(java.lang.String)
	 */
	@Override
	public Value<Double> fromString(String string) {
		if (string == null)
			return new Value<Double>(null, dbField);
		return new Value<Double>(Double.valueOf(string), dbField);
	}
}
