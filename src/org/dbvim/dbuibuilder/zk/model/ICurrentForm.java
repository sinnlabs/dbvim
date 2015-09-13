/**
 * 
 */
package org.dbvim.dbuibuilder.zk.model;

import org.dbvim.dbuibuilder.db.model.DBField;
import org.dbvim.dbuibuilder.model.Form;

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
