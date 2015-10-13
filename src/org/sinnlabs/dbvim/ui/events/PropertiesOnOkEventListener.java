/**
 * 
 */
package org.sinnlabs.dbvim.ui.events;

import org.sinnlabs.dbvim.zk.model.IDeveloperStudio;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;

/**
 * Listener for the designer properties
 * @author peter.liverovsky
 *
 */
public class PropertiesOnOkEventListener implements EventListener<Event> {
	
	private IDeveloperStudio developer;
	
	public PropertiesOnOkEventListener(IDeveloperStudio developer) {
		this.developer = developer;
	}
	
	@Override
	public void onEvent(Event event) throws Exception {
		// update the property
		developer.getDesignerProperties().updateProperty(event.getTarget());
		// set dirty flag
		developer.getDesignerCanvas().setDirty(true);
	}
}
