/**
 * 
 */
package org.sinnlabs.dbvim.ui;

import java.util.HashMap;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.sinnlabs.dbvim.ui.events.EventsExpandOnClickEventListener;
import org.sinnlabs.dbvim.ui.events.EventsOnOkEventListener;
import org.sinnlabs.dbvim.zk.model.ComponentFactory;
import org.sinnlabs.dbvim.zk.model.DeveloperFactory;
import org.sinnlabs.dbvim.zk.model.IDeveloperStudio;
import org.sinnlabs.dbvim.zk.model.ZUMLModel;
import org.zkoss.zk.ui.AbstractComponent;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.metainfo.ZScript;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.sys.ComponentCtrl;
import org.zkoss.zul.Button;
import org.zkoss.zul.Column;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;

/**
 * Implements a window that displays a selected designer Component's events and
 * allows the user to modify them
 * 
 * @author peter.liverovsky
 *
 */
public class DesignerEvents extends Groupbox implements IdSpace {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3726786629046025810L;

	/**
	 * Current component
	 */
	protected Component current = null;

	/**
	 * A map that contains all the Events values
	 */
	protected HashMap<Component, String> mapEvents = null;
	
	protected IDeveloperStudio developer;

	@Wire("#gridEvents")
	protected Grid gridEvents;
	
	@Wire
	protected Column colEvent;
	
	@Wire
	protected Column colHandler;

	public DesignerEvents() {
		
		developer = DeveloperFactory.getInstance();
		
		// create the ui
		Executions.createComponents("/components/ComponentEvents.zul", this,
				null);
		Selectors.wireComponents(this, this, false);

		setClosable(false);
		
		// Clear properties list
		clear();
	}

	protected void clear() {
		gridEvents.getRows().getChildren().clear();
	}

	/**
	 * Set the component to display the events
	 * 
	 * @param comp
	 *            - reference to the component
	 */
	public void setCurrent(Component comp) {
		current = comp;
		buildEvents();
	}

	/**
	 * Display the component events on a grid.
	 */
	protected void buildEvents() {
		if (current == null)
			return;

		clear();
		
		// set the window's title
		setTitle(ComponentFactory.getSimpleClassName(current) + " Events");

		// get all the events that apply to this component
		String[] arrEvents = ComponentFactory.getComponentEvents(current
				.getClass());

		if (ArrayUtils.isEmpty(arrEvents))
			return;
		
		// create property event listeners
		EventsOnOkEventListener listener = new EventsOnOkEventListener(developer);
		EventsExpandOnClickEventListener expandListener = 
				new EventsExpandOnClickEventListener(developer);

		mapEvents = new HashMap<Component, String>();

		// iterate through the events array
		for (String sEventName : arrEvents) {
			try {
				// get the next event's name
				sEventName = StringUtils.trim(sEventName);

				// check if this event has a defined event handler (script)
				// [we don't care about event listeners, as we are after ZUML
				// elements only - listeners are code elements]
				ZScript zScript = ((ComponentCtrl) current)
						.getEventHandler(sEventName);
				String sScript = zScript != null ? zScript.getContent(null,
						null) : "";

				// create a new Grid row
				Row row = new Row();
				gridEvents.getRows().appendChild(row);

				// add the Event name at the 1st column
				Label lblName = new Label();
				lblName.setValue(sEventName);
				row.appendChild(lblName);

				// add the Handler value at the 2nd column

				Hlayout layout = new Hlayout();
				layout.setValign("middle");
				
				
				// for any other property type, display a
				// textbox component
				Textbox textbox = new Textbox();
				textbox.setHflex("1");
				textbox.setMaxlength(1000);
				layout.appendChild(textbox);
				
				// button for expand box
				Button expand = new Button();
				expand.setLabel("...");
				layout.appendChild(expand);

				// if the size of the value string is > 50 chars,
				// set the Textbox to multiline, so that the value
				// is visible
				if (sScript.length() > 50)
					textbox.setMultiline(true);

				// display the value on the Textbox
				textbox.setValue(sScript);
				row.appendChild(layout);

				// add the Textbox objects to the Hashmap,
				// using their Ids as the key
				mapEvents.put(layout, sEventName);
				textbox.addEventListener(Events.ON_CHANGE, listener);
				textbox.addEventListener(Events.ON_OK, listener);
				expand.addEventListener(Events.ON_CLICK, expandListener);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		colEvent.setHflex("min");
		colHandler.setHflex("1");
	}
	
	/**
	 * 
	 */
	public void updateEventHandlers(Component cmpValue)
	{
		if ((mapEvents == null) || (mapEvents.isEmpty()))
			return;

		try
		{	
			// get the ZUML representation of the current model
			ZUMLModel model = developer.getDesignerCanvas().getZUMLRepresentation();
			
			if (model == null)
				return;
			
			String sEventName = (String) mapEvents.get(cmpValue);
							
			// get the event handling script (textbox value)
			String sScript = ((Textbox) cmpValue.getChildren().get(0)).getValue();
					
			if (sScript == null)
				sScript = "";
					
			// add the event handler directly to the iDOM element
			model.addEventHandler((AbstractComponent)current, sEventName, sScript);
			
			
			// reload the model with refreshing the tree, 
			// so that the updated event handlers will be activated
			developer.getDesignerCanvas().
				loadModelFromDocument(model.getZUMLDocument(), true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
