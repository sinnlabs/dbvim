/**
 * 
 */
package org.sinnlabs.dbvim.ui.modeltree;

import java.io.Serializable;

/**
 * @author peter.liverovsky
 *
 */
public class MenuTreeNode implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8001129951233199812L;
	
	
	private Object menu;
	
	public MenuTreeNode(Object menu) {
		this.menu = menu;
	}
	
public boolean isLeaf() { return true; }
	
	public Object getChild(int index) {
		return null;
	}
	
	public int getChildCount(Object node) {
		return 0;
	}
	
	public Object getMenu() {
		return menu;
	}
}
