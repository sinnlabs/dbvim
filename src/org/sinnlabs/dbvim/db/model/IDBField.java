/**
 * 
 */
package org.sinnlabs.dbvim.db.model;

/**
 * @author peter.liverovsky
 *
 */
public interface IDBField {
	
	public String getName();
	
	public String getDBTypeName();
	
	public String getTableName();
	
	public String getCatalogName();
	
	public String getFullName();
	
	public int getDBType();
	
	public boolean isGenerated();
	
	public boolean isNullable();
}