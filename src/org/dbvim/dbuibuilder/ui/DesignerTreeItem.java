/**
 * 
 */
package org.dbvim.dbuibuilder.ui;

import org.dbvim.dbuibuilder.zk.model.IComponentHolder;
import org.zkoss.zul.Treeitem;

/**
 * @author peter.liverovsky
 *
 */
public class DesignerTreeItem extends Treeitem implements IComponentHolder {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6450675082771958053L;
	
	protected String componentId = null;
	
	public DesignerTreeItem(String cmpId) {
		componentId = cmpId;
	}

	/* (non-Javadoc)
	 * @see com.asd.zkdesigner.model.IComponentHolder#getComponentId()
	 */
	@Override
	public String getComponentId() {
		return componentId;
	}

}
