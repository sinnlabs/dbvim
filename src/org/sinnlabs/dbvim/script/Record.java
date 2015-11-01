/**
 * 
 */
package org.sinnlabs.dbvim.script;

import java.util.HashMap;
import java.util.Map;

/**
 * Class represent db record.
 * Used in scripting
 * @author peter.liverovsky
 *
 */
public class Record {

	private Map<String, Object> values;
	
	public Record() {
		values = new HashMap<String, Object>();
	}
	
	public Map<String, Object> getValues() { return values; }
	
	/**
	 * Returns field value
	 * @param fieldId field id
	 * @return
	 */
	public Object get(String fieldId) {
		if (!values.containsKey(fieldId))
			throw new IllegalArgumentException("Field id: " + fieldId + " does not exists.");
		
		return values.get(fieldId);
	}
}
