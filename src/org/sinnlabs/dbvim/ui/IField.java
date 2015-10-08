/**
 * 
 */
package org.sinnlabs.dbvim.ui;

import java.util.Map;

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
	 * Returns DBField object
	 */
	public DBField getDBField();
	
	/**
	 * Used for set mapping when component loaded from ZUML
	 * @return
	 */
	public String getMapping();
	
	/**
	 * Used for set mapping when component loaded from ZUML
	 * @param map
	 */
	public void setMapping(String map) throws Exception;
	
	/**
	 * Used to set mapping to Join forms
	 * @param form Join form name
	 */
	public void setForm(String form);
	
	/**
	 * Used to set mapping to Join forms
	 * @return
	 */
	public String getForm();

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
	
	/**
	 * Returns field ID
	 * @return
	 */
	public String getId();
	
	/**
	 * Converts string to value
	 * @param string String to be converted. If string is null then null value returned.
	 * @return
	 */
	public Value<T> fromString(String string);
	
	/**
	 * Returns field label
	 */
	public String getLabel();
	
	/**
	 * Calls when all components attributes are loaded
	 * @param args - Creation parameters
	 * @throws Exception
	 */
	public void onCreate(Map<?,?> args) throws Exception;
}
