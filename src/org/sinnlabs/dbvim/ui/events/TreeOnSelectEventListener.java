/**
 * 
 */
package org.sinnlabs.dbvim.ui.events;

import org.sinnlabs.dbvim.ui.DesignerTreeItem;
import org.sinnlabs.dbvim.zk.model.IDeveloperStudio;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.SelectEvent;

/**
 * @author peter.liverovsky
 *
 */
@SuppressWarnings("rawtypes")
public class TreeOnSelectEventListener implements EventListener<SelectEvent> {

	private HtmlBasedComponent last = null;
	
	private IDeveloperStudio developer;
	
	public TreeOnSelectEventListener(IDeveloperStudio developer) {
		this.developer = developer;
	}
	
	@Override
	public void onEvent(SelectEvent event) throws Exception {
		if( event.getSelectedItems().size() == 0 )
			return;
		DesignerTreeItem item = (DesignerTreeItem) event.getSelectedItems().iterator().next();
		Component selectedComp = developer.getDesignerTree().
					getCorrespondingCanvasComponent(item);
		
		// highlight element
		HtmlBasedComponent c = (HtmlBasedComponent) selectedComp;
		c.setStyle("border: thin solid red;");
		if (event.getPreviousSelectedItems().size() != 0) {
			DesignerTreeItem prev = 
					(DesignerTreeItem) event.getPreviousSelectedItems().iterator().next();
			
			c = (HtmlBasedComponent) developer.getDesignerTree().
					getCorrespondingCanvasComponent(prev);
			if (c != null)
				c.setStyle("");
		}
		if (last != null) {
			last.setStyle("");
		}
		last = (HtmlBasedComponent) selectedComp;
		
		developer.getDesignerProperties().setCurrent(selectedComp);
		developer.getDesignerEvents().setCurrent(selectedComp);
	}
}
