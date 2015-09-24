/**
 * 
 */
package org.sinnlabs.dbvim.db;

import java.util.ArrayList;
import java.util.List;

/**
 * Class represents one row from db table
 * @author peter.liverovsky
 *
 */
public class Entry {

	protected List<Value<?>> id;
	
	protected List<Value<?>> values;
	
	public Entry() {
		values = new ArrayList<Value<?>>();
		id = new ArrayList<Value<?>>();
	}
	
	/**
	 * Gets db primary id values
	 */
	public List<Value<?>> getID() { return id; }
	
	public void setID(List<Value<?>> val) { id = val; }
	
	/**
	 * Gets column values
	 */
	public List<Value<?>> getValues() { return values; }
}
