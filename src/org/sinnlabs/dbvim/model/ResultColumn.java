/**
 * 
 */
package org.sinnlabs.dbvim.model;

import java.io.Serializable;

/**
 * Class represents form result list column
 * @author peter.liverovsky
 *
 */
public class ResultColumn implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6993458911455745391L;

	public String fieldName;
	
	public String label;
	
	public ResultColumn() {
		this("", "");
	}
	
	public ResultColumn(String field) {
		this(field, field);
	}
	
	public ResultColumn(String field, String label) {
		this.fieldName = field;
		this.label = label;
	}
}
