/**
 * 
 */
package org.sinnlabs.dbvim.zk.model;

import java.lang.reflect.Method;
import org.apache.commons.collections.map.MultiValueMap;
import org.sinnlabs.dbvim.ui.annotations.WireEvent;

/**
 * Class that process annotations for the object
 * @author peter.liverovsky
 *
 */
/*package*/ class AnnotationProcessor {

	private Object target;
	
	private MultiValueMap events;
	
	public AnnotationProcessor(Object target) {
		this.target = target;
		events = new MultiValueMap();
		
		readAnnotations();
	}
	
	private void readAnnotations() {
		if (target == null)
			return;
		
		Method[] methods = target.getClass().getMethods();
		
		for (Method m : methods) {
			WireEvent annotation = m.getAnnotation(WireEvent.class);
			if (annotation != null) {
				events.put(annotation.value(), m);
			}
		}
	}
	
	public MultiValueMap getEvents() { return events; }
}
