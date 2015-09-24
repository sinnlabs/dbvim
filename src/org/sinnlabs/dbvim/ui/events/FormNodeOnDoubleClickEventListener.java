/**
 * 
 */
package org.sinnlabs.dbvim.ui.events;

import org.sinnlabs.dbvim.model.Form;
import org.sinnlabs.dbvim.ui.modeltree.FormTreeNode;
import org.sinnlabs.dbvim.zk.model.DeveloperFactory;
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
