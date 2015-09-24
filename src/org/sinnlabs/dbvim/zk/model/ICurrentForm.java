/**
 * 
 */
package org.sinnlabs.dbvim.zk.model;

import org.sinnlabs.dbvim.db.model.DBField;
import org.sinnlabs.dbvim.model.Form;

/**
 * @author peter.liverovsky
 *
 */
public interface ICurrentForm {
	/**
	 * Get current form
	 */
	public Form getForm();
	
	/**
	 * Returns DB field object by name
	 * @param name DB column name
	 * @return DBField object for column
	 */
	public DBField getDBFieldByMapping(String name);
}
