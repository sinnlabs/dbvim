/**
 * 
 */
package org.sinnlabs.dbvim.ui.events;

import org.sinnlabs.dbvim.zk.model.IDeveloperStudio;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;

/**
 * Listener for the designer events
 * @author peter.liverovsky
 *
 */
public class EventsOnOkEventListener implements EventListener<Event> {
	
	private IDeveloperStudio developer;
	
	public EventsOnOkEventListener(IDeveloperStudio developer) {
		this.developer = developer;
	}

	@Override
	public void onEvent(Event event) throws Exception {
		// update the event handler
		developer.getDesignerEvents().updateEventHandlers(event.getTarget().getParent());
		// set dirty flag
		developer.getDesignerCanvas().setDirty(true);
	}

}
