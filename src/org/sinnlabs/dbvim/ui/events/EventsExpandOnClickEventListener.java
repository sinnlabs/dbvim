/**
 * 
 */
package org.sinnlabs.dbvim.ui.events;

import org.sinnlabs.dbvim.ui.ExpandWindow;
import org.sinnlabs.dbvim.zk.model.IDeveloperStudio;
import org.sinnlabs.zk.ui.CodeMirror;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Textbox;

/**
 * @author peter.liverovsky
 *
 */
public class EventsExpandOnClickEventListener implements EventListener<Event> {

	private IDeveloperStudio developer;
	
	public EventsExpandOnClickEventListener(IDeveloperStudio developer) {
		this.developer = developer;
	}

	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.event.EventListener#onEvent(org.zkoss.zk.ui.event.Event)
	 */
	@Override
	public void onEvent(Event evnt) throws Exception {
		final ExpandWindow wnd = new ExpandWindow();
		developer.getDesigner().appendChild(wnd);
		
		final Hlayout layout = (Hlayout) evnt.getTarget().getParent();
		final Textbox txtEvent = (Textbox) layout.getChildren().get(0);
		
		//wnd.setText(Roaster.format(txtEvent.getText()));
		wnd.setMode(CodeMirror.JAVA);
		
		wnd.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

			@Override
			public void onEvent(Event arg0) throws Exception {
				if (wnd.getSelectedAction() == ExpandWindow.DD_OK) {
					// update event textbox
					txtEvent.setText(wnd.getText());
					// update the event handler
					developer.getDesignerEvents().updateEventHandlers(layout);
					// set dirty flag
					developer.getDesignerCanvas().setDirty(true);
				}
			}
			
		});
		wnd.doModal();
	}
}
