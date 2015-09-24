/**
 * 
 */
package org.sinnlabs.dbvim.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.mysql.jdbc.StringUtils;

/**
 * Class that represents View for db table
 * @author peter.liverovsky
 *
 */
@DatabaseTable(tableName = "dbForms")
public class Form {
	
	public static final String NAME_FIELD_NAME = "name";
	public static final String CONNECTION_FIELD_NAME = "connection_id";
	
	@DatabaseField(id = true)
	protected String name;
	
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
	protected DBConnection connection;
	
	@DatabaseField
	protected String dbTableName;
	
	@DatabaseField
	protected String catalogName;
	
	@DatabaseField
	protected String sResultList;
	
	@DatabaseField(width = 7999)
	protected String view;
	
	@DatabaseField
	protected String title;
	
	/* Getters and Setters */
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public DBConnection getDBConnection() { return connection; }
	public void setDBConnection(DBConnection conn) { connection = conn; }
	public String getTableName() { return dbTableName; }
	public void setTableName(String tableName) { dbTableName = tableName; }
	public String getCatalog() { return catalogName; }
	public void setCatalog(String catalog) { catalogName = catalog; }
	public String getView() { return view; }
	public void setView(String view) { this.view = view; }
	public String getsResultList() { return sResultList; }
	public void setsResultList(String sResultList) { this.sResultList = sResultList; }
	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }
	
	public String[] getResultList() {
		if (this.sResultList == null) {
			return null;
		}
		
		String[] res = this.sResultList.split(";");
		return res;
	}
	
	public String getQualifiedName() {
		String res = dbTableName;
		if ( !StringUtils.isNullOrEmpty(catalogName) ) {
			res = catalogName + "." + res;
		}
		return res;
	}
	
	@Override
	public String toString() {
		return dbTableName + "(" + catalogName + ")";
	}
}