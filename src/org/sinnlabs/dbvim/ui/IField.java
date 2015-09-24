/**
 * 
 */
package org.sinnlabs.dbvim.ui;

import org.sinnlabs.dbvim.db.Value;
import org.sinnlabs.dbvim.db.model.DBField;

/**
 * @author peter.liverovsky
 *
 */
public interface IField<T> {
	
	public static final int MODE_SEARCH=0;
	
	public static final int MODE_MODIFY=1;
	
	/**
	 * Use to set mapping
	 * @param field
	 */
	public void setDBField(DBField field);
	
	/**
	 * Used for set mapping when component loaded from ZUML
	 * @return
	 */
	public String getMapping();
	
	/**
	 * Used for set mapping when component loaded from ZUML
	 * @param map
	 */
	public void setMapping(String map);

	/**
	 * Set the field value
	 * @param v
	 */
	public void setDBValue(Value<T> v);
	
	/**
	 * Returns field db value
	 * @return value or null if value not set
	 */
	public Value<T> getDBValue();
	
	/**
	 * Show error on the client
	 */
	public void setErrorMessage(String err);
	
	/**
	 * Clear error on the client
	 */
	public void clearErrorMessage();
	
	/**
	 * Sets the field mode
	 * @param mode - Field mode (search, modify)
	 */
	public void setFieldMode(int mode);
}
