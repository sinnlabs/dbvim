/**
 * 
 */
package org.sinnlabs.dbvim.ui.events;

import org.sinnlabs.dbvim.model.Form;
import org.sinnlabs.dbvim.ui.modeltree.FormTreeNode;
import org.sinnlabs.dbvim.zk.model.IDeveloperStudio;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zul.Treeitem;

/**
 * @author peter.liverovsky
 *
 */
public class FormNodeOnDoubleClickEventListener implements EventListener<MouseEvent> {
	
	private IDeveloperStudio developer;
	
	public FormNodeOnDoubleClickEventListener(IDeveloperStudio studio) {
		developer = studio;
	}

	@Override
	public void onEvent(MouseEvent evnt) throws Exception {
		Treeitem item = (Treeitem) evnt.getTarget();
		FormTreeNode node = (FormTreeNode) item.getValue();
		Form form = node.getForm();
		developer.FormTreeNode_onDoubleClick(form);
	}

}
