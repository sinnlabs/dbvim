/**
 * 
 */
package org.sinnlabs.dbvim.ui.db;

import java.math.BigDecimal;
import org.sinnlabs.dbvim.db.Value;
import org.sinnlabs.dbvim.db.model.DBField;
import org.zkoss.zul.Decimalbox;

/**
 * @author peter.liverovsky
 *
 */
public class DecimalField extends BaseField<BigDecimal, Decimalbox> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1974306758258463281L;

	public DecimalField(DBField field) {
		super("/components/decimalfield.zul", field);
	}
	
	
	public DecimalField() {
		super("/components/decimalfield.zul", null);
	}


	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.ui.IField#fromString(java.lang.String)
	 */
	@Override
	public Value<BigDecimal> fromString(String string) {
		if (string == null)
			return new Value<BigDecimal>(null, dbField);
		return new Value<BigDecimal>(new BigDecimal(string), dbField);
	}
	

}
