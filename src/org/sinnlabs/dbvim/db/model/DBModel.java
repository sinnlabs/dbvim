/**
 * 
 */
package org.sinnlabs.dbvim.db.model;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author peter.liverovsky
 *
 */
public class DBModel {

	private Connection jdbc;
	
	public DBModel(String connectionString, String className) throws SQLException, ClassNotFoundException {
		/* force load jdbc driver */
		Class.forName(className);
		jdbc = DriverManager.getConnection(connectionString);
	}
	
	public List<DBTable> getTables() throws SQLException {
		ArrayList<DBTable> list = new ArrayList<DBTable>();
		
		ResultSet tables = jdbc.getMetaData().getTables(jdbc.getCatalog(), null, null, new String[] {"TABLE"});
		while(tables.next()) {
			list.add(new DBTable(tables.getString(2), tables.getString(3)));
		}
		
		return list;
	}
	
	/**
	 * Returns all fields from table
	 * @param catalog - schema name
	 * @param tablename - table name
	 * @return List of DBFields
	 * @throws SQLException
	 */
	public List<DBField> getFields(String catalog, String tablename) throws SQLException {
		List<DBField> list = new ArrayList<DBField>();
		
		ResultSet columns = jdbc.getMetaData().getColumns(jdbc.getCatalog(), 
				catalog, tablename, null);
		while(columns.next()) {
			boolean nullable = true;
			if (columns.getInt("NULLABLE") == DatabaseMetaData.columnNoNulls) {
				nullable = false;
			}
			list.add(new DBField(columns.getString("COLUMN_NAME"), tablename, catalog,
					columns.getString("TYPE_NAME"), columns.getInt("DATA_TYPE"), 
					isGenerated(columns), isPrimaryKey(catalog, 
							tablename, columns.getString("COLUMN_NAME")), nullable));
		}
		return list;
	}
	
	/**
	 * Checks whether the column is primary key
	 * @param catalog - schema name
	 * @param tablename - table name
	 * @param column - column name
	 * @return True is column is primary key, otherwise false
	 * @throws SQLException
	 */
	public boolean isPrimaryKey(String catalog, String tablename, 
			String column) throws SQLException {
		
		ResultSet columns = jdbc.getMetaData().getPrimaryKeys(jdbc.getCatalog(), 
				catalog, tablename);
		
		while(columns.next()) {
			if ( columns.getString("COLUMN_NAME").equals(column))
				return true;
		}
		
		return false;
	}
	
	public DBField getField(String catalog, String tableName, String fieldname) throws SQLException {
		ResultSet columns = jdbc.getMetaData().getColumns(jdbc.getCatalog(), catalog, tableName, fieldname);
		if (columns.next()) {
			boolean nullable = true;
			if (columns.getInt("NULLABLE") == DatabaseMetaData.columnNoNulls) {
				nullable = false;
			}
			return new DBField(columns.getString("COLUMN_NAME"), tableName, catalog,
					columns.getString("TYPE_NAME"), columns.getInt("DATA_TYPE"), 
					isGenerated(columns), isPrimaryKey(catalog, 
							tableName, columns.getString("COLUMN_NAME")), nullable);
		}
		return null;
	}
	
	/**
	 * Checks weather the column is auto generated
	 * @param col - ResultSet
	 * @return True is column is auto generated, otherwise false
	 * @throws SQLException
	 */
	private boolean isGenerated(ResultSet col) throws SQLException {
		// most jdbc driver support IS_AUTOINCREMENT column
		if (col.getString("IS_AUTOINCREMENT").equals("YES"))
			return true;
		return false;
	}
}
