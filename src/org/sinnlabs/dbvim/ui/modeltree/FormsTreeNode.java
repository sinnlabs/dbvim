/**
 * 
 */
package org.sinnlabs.dbvim.ui.modeltree;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;

import org.sinnlabs.dbvim.config.ConfigLoader;
import org.sinnlabs.dbvim.model.DBConnection;
import org.sinnlabs.dbvim.model.Form;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

/**
 * @author peter.liverovsky
 *
 */
public class FormsTreeNode implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8107857669265459110L;
	private DBConnection connection;
	private List<Form> forms;
	
	public FormsTreeNode(DBConnection conn) throws SQLException {
		connection = conn;
		QueryBuilder<Form, String> qb = ConfigLoader.getInstance().getForms().queryBuilder();
		Where<Form, String> w = qb.where();
		w.eq(Form.CONNECTION_FIELD_NAME, connection.getName());
		forms = ConfigLoader.getInstance().getForms().query(qb.prepare());
	}

	public boolean isLeaf() {
		return false;
	}
	
	public Object getChild(int index) {
		return new FormTreeNode(forms.get(index));
	}
	
	public int getChildCount() {
		return forms.size();
	}
}
