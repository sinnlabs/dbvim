/**
 * 
 */
package org.dbvim.dbuibuilder.ui.events;

import org.dbvim.dbuibuilder.zk.model.DeveloperFactory;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;

/**
 * Listener for the designer events
 * @author peter.liverovsky
 *
 */
public class EventsOnOkEventListener implements EventListener<Event> {

	@Override
	public void onEvent(Event event) throws Exception {
		// update the event handler
		DeveloperFactory.getInstance().getDesignerEvents().updateEventHandlers(event.getTarget());
		// set dirty flag
		DeveloperFactory.getInstance().getDesignerCanvas().setDirty(true);
	}

}
