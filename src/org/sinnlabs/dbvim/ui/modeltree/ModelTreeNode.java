/**
 * 
 */
package org.sinnlabs.dbvim.ui.modeltree;

import java.sql.SQLException;
import java.util.List;

import org.sinnlabs.dbvim.config.ConfigLoader;
import org.sinnlabs.dbvim.model.DBConnection;
import org.zkoss.zul.AbstractTreeModel;
import org.zkoss.zul.Messagebox;

/**
 * @author peter
 *
 */
public class ModelTreeNode extends AbstractTreeModel<Object> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -184379510873779493L;
	
	private List<DBConnection> connections;

	public ModelTreeNode() throws SQLException {
		super("Root");
		// TODO Auto-generated constructor stub
		connections = ConfigLoader.getInstance().getDBConnections().queryForAll();
	}

	/* (non-Javadoc)
	 * @see org.zkoss.zul.TreeModel#getChild(java.lang.Object, int)
	 */
	@Override
	public Object getChild(Object node, int index) {
		if (node instanceof String) {
			return connections.get(index);
		}
		if (node instanceof DBConnection) {
			DBConnection conn = (DBConnection) node;
			if (index == 0) {
				try {
					return new TablesTreeNode(conn);
				} catch (ClassNotFoundException | SQLException e) {
					Messagebox.show("Unable to get list of tables: " + e.getMessage(), 
							"Error", Messagebox.OK, Messagebox.ERROR);
					System.err.println("Unable to get list of tables: " + e.getMessage());
					e.printStackTrace();
				}
			}
			else if (index == 1) {
				try {
					return new FormsTreeNode(conn);
				} catch (SQLException e) {
					Messagebox.show("Unable to get list of forms: " + e.getMessage(), 
							"Error", Messagebox.OK, Messagebox.ERROR);
					System.err.println("Unable to get list of forms: " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
		if (node instanceof TablesTreeNode) {
			TablesTreeNode tables = (TablesTreeNode) node;
			return tables.getChild(index);
		}
		if (node instanceof FormsTreeNode) {
			FormsTreeNode forms = (FormsTreeNode) node;
			return forms.getChild(index);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.zkoss.zul.TreeModel#getChildCount(java.lang.Object)
	 */
	@Override
	public int getChildCount(Object node) {
		if (node instanceof String) {
			return connections.size();
		}
		if (node instanceof DBConnection) {
			return 2;
		}
		if (node instanceof TablesTreeNode) {
			TablesTreeNode tables = (TablesTreeNode) node;
			return tables.getCount();
		}
		if (node instanceof FormsTreeNode) {
			FormsTreeNode forms = (FormsTreeNode) node;
			return forms.getChildCount();
		}
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.zkoss.zul.TreeModel#isLeaf(java.lang.Object)
	 */
	@Override
	public boolean isLeaf(Object node) {
		if (node instanceof DBConnection) {
			return false;
		}
		if (node instanceof String) {
			return false;
		}
		if (node instanceof TablesTreeNode) {
			return false;
		}
		if (node instanceof FormsTreeNode) {
			return false;
		}
		return true;
	}
}
