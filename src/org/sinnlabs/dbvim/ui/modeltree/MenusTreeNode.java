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
import org.sinnlabs.dbvim.model.SearchMenu;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

/**
 * @author peter.liverovsky
 *
 */
public class MenusTreeNode implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8821032822201751576L;

	
	private List<SearchMenu> menus;
	
	public MenusTreeNode(DBConnection connection) throws SQLException {
		// Get list of forms
		QueryBuilder<Form, String> qb = ConfigLoader.getInstance().getForms().queryBuilder();
		Where<Form, String> w = qb.where();
		w.eq(Form.CONNECTION_FIELD_NAME, connection.getName());
		qb.selectColumns(Form.NAME_FIELD_NAME);
		
		// Get list of menus
		QueryBuilder<SearchMenu, String> queryBuilder = 
				ConfigLoader.getInstance().getSearchMenus().queryBuilder();
		
		queryBuilder.where().in(SearchMenu.FORM_FIELD_NAME, qb);
		
		menus = queryBuilder.query();
	}
	
	public boolean isLeaf() {
		return false;
	}
	
	public Object getChild(int index) {
		return new MenuTreeNode(menus.get(index));
	}
	
	public int getChildCount() {
		return menus.size();
	}
}
