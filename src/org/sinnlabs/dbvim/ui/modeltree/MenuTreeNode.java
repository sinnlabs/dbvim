/**
 * 
 */
package org.sinnlabs.dbvim.ui.modeltree;

import java.io.Serializable;

import org.sinnlabs.dbvim.model.SearchMenu;

/**
 * @author peter.liverovsky
 *
 */
public class MenuTreeNode implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8001129951233199812L;
	
	
	private SearchMenu menu;
	
	public MenuTreeNode(SearchMenu menu) {
		this.menu = menu;
	}
	
public boolean isLeaf() { return true; }
	
	public Object getChild(int index) {
		return null;
	}
	
	public int getChildCount(Object node) {
		return 0;
	}
	
	public SearchMenu getMenu() {
		return menu;
	}
}
