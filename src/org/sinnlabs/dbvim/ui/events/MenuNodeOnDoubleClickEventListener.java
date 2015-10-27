/**
 * 
 */
package org.sinnlabs.dbvim.ui.events;

import org.sinnlabs.dbvim.model.SearchMenu;
import org.sinnlabs.dbvim.ui.modeltree.MenuTreeNode;
import org.sinnlabs.dbvim.zk.model.IDeveloperStudio;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zul.Treeitem;

/**
 * @author peter.liverovsky
 *
 */
public class MenuNodeOnDoubleClickEventListener implements
		EventListener<MouseEvent> {
	
	private IDeveloperStudio developer;
	
	public MenuNodeOnDoubleClickEventListener(IDeveloperStudio developer) {
		this.developer = developer;
	}

	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.event.EventListener#onEvent(org.zkoss.zk.ui.event.Event)
	 */
	@Override
	public void onEvent(MouseEvent evnt) throws Exception {
		Treeitem item = (Treeitem) evnt.getTarget();
		MenuTreeNode node = (MenuTreeNode) item.getValue();
		SearchMenu menu = node.getMenu();
		developer.MenuTreeNode_onDoubleClick(menu);
	}

}
