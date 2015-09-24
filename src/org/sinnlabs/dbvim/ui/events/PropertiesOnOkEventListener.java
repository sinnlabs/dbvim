/**
 * 
 */
package org.sinnlabs.dbvim.ui.events;

import org.sinnlabs.dbvim.zk.model.DeveloperFactory;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;

/**
 * Listener for the designer properties
 * @author peter.liverovsky
 *
 */
public class PropertiesOnOkEventListener implements EventListener<Event> {
	
	public PropertiesOnOkEventListener() {
		
	}
	
	@Override
	public void onEvent(Event event) throws Exception {
		// update the property
		DeveloperFactory.getInstance().getDesignerProperties().updateProperty(event.getTarget());
		// set dirty flag
		DeveloperFactory.getInstance().getDesignerCanvas().setDirty(true);
	}
}
