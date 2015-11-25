/**
 * 
 */
package org.sinnlabs.dbvim.ui.events;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;

/**
 * Raise when user deletes component
 * @author peter.liverovsky
 *
 */
public class ComponentDeletedEvent extends Event {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4536648900420662037L;

	private Component deleted;
	
	/**
	 * Creates instance
	 * @param target Target component
	 * @param deletedComponent Deleted component form the ModelTree
	 */
	public ComponentDeletedEvent(Component target, Component deletedComponent) {
		super(DeveloperEvents.ON_COMPONENT_DELETED, target);
		deleted = deletedComponent;
	}
	
	/**
	 * Returns deleted component
	 * @return
	 */
	public Component getDeletedComponent() {
		return deleted;
	}

}
