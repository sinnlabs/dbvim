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
	
	protected int type;
	
	protected boolean isGenerated;
	
	protected boolean isPrimaryKey;
	
	protected boolean isNullable;
	
	public String getName() { return name; }
	
	public String getDBTypeName() { return dbType; }
	
	public int getDBType() { return type; }
	
	public boolean isGenerated() { return isGenerated; }
	
	public boolean isPrimaryKey() { return isPrimaryKey; }
	
	public DBField(String name, String type, int dbtype, boolean generated, 
			boolean primarykey, boolean nullable) {
		this.name = name;
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
}