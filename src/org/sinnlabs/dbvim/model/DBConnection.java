/**
 * 
 */
package org.sinnlabs.dbvim.model;

import java.io.Serializable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * @author peter.liverovsky
 *
 */

@DatabaseTable(tableName = "jdbcConnections")
public class DBConnection implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4402828179976378473L;

	@DatabaseField(id = true)
	private String name;
	
	@DatabaseField
	private String jdbcConnectionString;
	
	@DatabaseField
	private String className;
	
	public DBConnection() { 
		
	}
	
	public DBConnection(String name) {
		this(name, "", "");
	}
	
	public DBConnection(String name, String connectionString, String className) {
		this.name = name;
		this.jdbcConnectionString = connectionString;
		this.className = className;
	}
	
	public String getName() { return name; }
	
	public String getConnectionString() { return jdbcConnectionString; }
	
	public String getClassName() { return className; }
	
	public void setConnectionString(String str) { jdbcConnectionString = str; }
	
	public void setClassName(String str) { className = str; }
}
