/**
 * 
 */
package org.dbvim.dbuibuilder.ui.modeltree;

import java.io.Serializable;

import org.dbvim.dbuibuilder.model.Form;

/**
 * @author peter.liverovsky
 *
 */
public class FormTreeNode implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -727731152289389999L;
	private Form form;
	
	public FormTreeNode(Form frm) {
		form = frm;
	}
	
	public boolean isLeaf() { return true; }
	
	public Object getChild(int index) {
		return null;
	}
	
	public int getChildCount(Object node) {
		return 0;
	}
	
	public Form getForm() {
		return form;
	}
}
