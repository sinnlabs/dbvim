/**
 * 
 */
package org.dbvim.dbuibuilder.ui.modeltree;

import java.io.Serializable;

import org.dbvim.dbuibuilder.db.model.DBModel;
import org.dbvim.dbuibuilder.db.model.DBTable;
import org.dbvim.dbuibuilder.model.DBConnection;

/**
 * @author peter.liverovsky
 *
 */
public class TableTreeNode implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4930943243522363450L;

	protected DBModel model;
	
	protected int id;
	
	protected DBTable table;
	
	protected DBConnection connection;
	
	/* Getters and setters */
	public DBConnection getConnection() { return connection; }
	public DBTable getTable() { return table; }
	
	public TableTreeNode(DBModel model, int id, DBTable table, DBConnection c) {
		this.model = model;
		this.id = id;
		this.table = table;
		connection = c;
	}
}
