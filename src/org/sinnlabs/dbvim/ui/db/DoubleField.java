/**
 * 
 */
package org.sinnlabs.dbvim.ui.db;

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
}
