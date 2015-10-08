/**
 * 
 */
package org.sinnlabs.dbvim.db.model;

/**
 * @author peter.liverovsky
 *
 */
public class DBField implements IDBField {

	protected String name;
	
	protected String dbType;
	
	protected String tableName;
	
	protected String catalogName;
	
	protected int type;
	
	protected boolean isGenerated;
	
	protected boolean isPrimaryKey;
	
	protected boolean isNullable;
	
	public String getName() { return name; }
	
	public String getTableName() { return tableName; }
	
	public String getCatalogName() { return catalogName; }
	
	public String getDBTypeName() { return dbType; }
	
	public int getDBType() { return type; }
	
	public boolean isGenerated() { return isGenerated; }
	
	public boolean isPrimaryKey() { return isPrimaryKey; }
	
	public DBField(String name, String tableName, String catalogName, String type, int dbtype, boolean generated, 
			boolean primarykey, boolean nullable) {
		this.name = name;
		this.tableName = tableName;
		this.catalogName = catalogName;
		dbType = type;
		this.type = dbtype;
		isGenerated = generated;
		isPrimaryKey = primarykey;
		isNullable = nullable;
	}

	@Override
	public boolean isNullable() {
		return isNullable;
	}
	
	@Override
	public String getFullName() {
		String full = "";
		if (catalogName != null)
			full += catalogName + ".";
		if (tableName != null) {
			full += tableName + ".";
		}
		full += name;
		return full;
	}
}