/**
 * 
 */
package org.dbvim.dbuibuilder.ui.db;

import java.math.BigDecimal;

import org.dbvim.dbuibuilder.db.model.DBField;
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
	

}
