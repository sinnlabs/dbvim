/**
 * 
 */
package org.sinnlabs.dbvim.db;

import org.sinnlabs.dbvim.db.model.DBField;

/**
 * Class represents db column value
 * @author peter.liverovsky
 *
 */
public class Value<T> {

	protected DBField dbField;
	
	protected T value;
	
	public Value(T val, DBField f) {
		value = val;
		dbField = f;
	}
	
	public DBField getDBField() { return dbField; }
	
	public T getValue() { return value; }
}
