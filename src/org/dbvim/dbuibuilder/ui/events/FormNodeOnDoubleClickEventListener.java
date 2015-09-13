/**
 * 
 */
package org.dbvim.dbuibuilder.ui.events;

import org.dbvim.dbuibuilder.model.Form;
import org.dbvim.dbuibuilder.ui.modeltree.FormTreeNode;
import org.dbvim.dbuibuilder.zk.model.DeveloperFactory;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zul.Treeitem;

/**
 * @author peter.liverovsky
 *
 */
public class FormNodeOnDoubleClickEventListener implements EventListener<MouseEvent> {

	@Override
	public void onEvent(MouseEvent evnt) throws Exception {
		Treeitem item = (Treeitem) evnt.getTarget();
		FormTreeNode node = (FormTreeNode) item.getValue();
		Form form = node.getForm();
		DeveloperFactory.getInstance().FormTreeNode_onDoubleClick(form);
	}

}
