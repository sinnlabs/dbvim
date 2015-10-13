/**
 * 
 */
package org.sinnlabs.dbvim.ui;

import java.util.List;

import org.sinnlabs.dbvim.db.Database;

/**
 * Interface represents TableField
 * @author peter.liverovsky
 *
 */
public interface ITableField {
	public static final int MODE_SEARCH=0;
	
	public static final int MODE_MODIFY=1;
	
	/**
	 * Sets the field mode
	 * @param mode - Field mode (search, modify)
	 */
	public void setFieldMode(int mode);
	
	/**
	 * Returns field ID
	 * @return
	 */
	public String getId();
	
	/**
	 * updates table field content
	 * @param context Current context
	 * @param db Current Database instance
	 * @throws Exception 
	 */
	public void updateTable(List<IField<?>> context, Database db) throws Exception;
}
