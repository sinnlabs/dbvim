/**
 * 
 */
package org.sinnlabs.dbvim.ui.events;

import org.sinnlabs.dbvim.menu.MenuItem;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;

/**
 * Class represents onMenuSelected event
 * @author peter.liverovsky
 *
 */
public class MenuSelectEvent extends Event {
	/**
	 * 
	 */
	private static final long serialVersionUID = 9191400982940848686L;

	private MenuItem data;

	/**
	 * @param name
	 * @param target
	 * @param data
	 */
	public MenuSelectEvent(String name, Component target, MenuItem data) {
		super(name, target);
		this.data = data;
	}
	
	public MenuSelectEvent(String name, Component target) {
		super(name, target);
	}
	
	public MenuSelectEvent(String name) {
		super(name);
	}
	
	public String getLabel() {
		return data.getLabel().toString();
	}
	
	public Object getValue() {
		return data.getValue();
	}
}
