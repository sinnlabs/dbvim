/**
 * 
 */
package org.sinnlabs.dbvim.db.model;

/**
 * Interface that represents database field/column properties
 * @author peter.liverovsky
 *
 */
public interface IDBField {
	
	/**
	 * Returns db field name
	 */
	public String getName();
	
	/**
	 * Returns string that represents db type name
	 */
	public String getDBTypeName();
	
	/**
	 * Returns db table name
	 */
	public String getTableName();
	
	/**
	 * Returns db catalog
	 */
	public String getCatalogName();
	
	/**
	 * Returns qualified field name:
	 * [CatalogName].TableName.DBFieldName
	 * @return
	 */
	public String getFullName();
	
	/**
	 * Returns java.Sql type
	 */
	public int getDBType();
	
	/**
	 * Indicates that field value is automatically generated.
	 * For example: MSSQL Identity fields
	 * @return true if field is auto generetad, otherwise false
	 */
	public boolean isGenerated();
	
	/**
	 * Indicates that field can be set to null
	 * @return true if field can be set to null, otherwise false
	 */
	public boolean isNullable();
}
