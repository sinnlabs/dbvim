/**
 * 
 */
package org.sinnlabs.dbvim.ui.modeltree;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import org.sinnlabs.dbvim.db.model.DBModel;
import org.sinnlabs.dbvim.db.model.DBTable;
import org.sinnlabs.dbvim.model.DBConnection;

/**
 * @author peter.liverovsky
 *
 */
public class TablesTreeNode implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9155405209702331743L;

	protected DBConnection connection;
	
	protected DBModel model;
	
	protected List<DBTable> tables;
	
	public TablesTreeNode(DBConnection c) 
			throws ClassNotFoundException, SQLException {
		connection = c;
		model = new DBModel(c.getConnectionString(), c.getClassName());
		tables = model.getTables();
	}
	
	public boolean isLeaf() throws SQLException {
		if (tables.size() > 0)
			return false;
		return true;
	}
	
	public Object getChild(int index) {
		if (index < 0)
			return null;
		if (index >= tables.size())
			return null;
		
		return new TableTreeNode(model, index, tables.get(index), connection);
	}
	
	public int getCount() {
		return tables.size();
	}
}
