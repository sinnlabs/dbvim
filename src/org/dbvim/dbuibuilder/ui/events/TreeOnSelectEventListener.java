/**
 * 
 */
package org.dbvim.dbuibuilder.ui.events;

import org.dbvim.dbuibuilder.ui.DesignerTreeItem;
import org.dbvim.dbuibuilder.zk.model.DeveloperFactory;
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
	
	@Override
	public void onEvent(SelectEvent event) throws Exception {
		if( event.getSelectedItems().size() == 0 )
			return;
		DesignerTreeItem item = (DesignerTreeItem) event.getSelectedItems().iterator().next();
		Component selectedComp = 
				DeveloperFactory.getInstance().getDesignerTree().
					getCorrespondingCanvasComponent(item);
		
		// highlight element
		HtmlBasedComponent c = (HtmlBasedComponent) selectedComp;
		c.setStyle("border: thin solid red;");
		if (event.getPreviousSelectedItems().size() != 0) {
			DesignerTreeItem prev = 
					(DesignerTreeItem) event.getPreviousSelectedItems().iterator().next();
			
			c = (HtmlBasedComponent) DeveloperFactory.getInstance().getDesignerTree().
					getCorrespondingCanvasComponent(prev);
			if (c != null)
				c.setStyle("");
		}
		if (last != null) {
			last.setStyle("");
		}
		last = (HtmlBasedComponent) selectedComp;
		
		DeveloperFactory.getInstance().getDesignerProperties().setCurrent(selectedComp);
		DeveloperFactory.getInstance().getDesignerEvents().setCurrent(selectedComp);
	}

}
