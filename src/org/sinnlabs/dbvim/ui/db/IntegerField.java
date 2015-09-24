/**
 * 
 */
package org.sinnlabs.dbvim.ui.db;

import org.sinnlabs.dbvim.db.model.DBField;
import org.zkoss.zul.Intbox;

/**
 * @author peter.liverovsky
 *
 */
public class IntegerField extends BaseField<Integer, Intbox> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5904828500284669425L;

	public IntegerField(DBField dbfield) {
		super("/components/integerfield.zul", dbfield);
	}
	
	public IntegerField() {
		this(null);
	}
}
