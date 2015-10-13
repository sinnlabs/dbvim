/**
 * 
 */
package org.sinnlabs.dbvim.zk.model;

import java.lang.reflect.Method;
import java.util.Collection;

import org.apache.commons.collections.map.MultiValueMap;
import org.sinnlabs.dbvim.ui.annotations.EventType;

/**
 * Class process all wired form events
 * @author peter.liverovsky
 *
 */
public class FormEventProcessor {

	private MultiValueMap events;
	
	public FormEventProcessor() {
		events = new MultiValueMap();
	}
	
	public void addListeners(Object target) {
		AnnotationProcessor annProc = new AnnotationProcessor(target);
		
		for(Object key : annProc.getEvents().keySet()) {
			EventType type = (EventType) key;
			for (Object m : annProc.getEvents().getCollection(type)) {
				EventDescription event = new EventDescription();
				event.target = target;
				event.listener = (Method)m;
				events.put(type, event);
			}
		}
	}
	
	public void Invoke(EventType type, Object...objects) throws Exception {
		Collection<?> collection = events.getCollection(type);
		if (collection == null)
			return;
		
		for (Object e : collection) {
			EventDescription event = (EventDescription) e;
			if (objects == null)
				event.listener.invoke(event.target);
			else
				event.listener.invoke(event.target, objects);
		}
	}
	
	private class EventDescription {
		public Object target;
		public Method listener;
	}
}
