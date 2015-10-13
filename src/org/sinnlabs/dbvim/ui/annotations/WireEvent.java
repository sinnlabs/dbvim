/**
 * 
 */
package org.sinnlabs.dbvim.ui.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author peter.liverovsky
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WireEvent {
	EventType value() default EventType.NONE;
}
