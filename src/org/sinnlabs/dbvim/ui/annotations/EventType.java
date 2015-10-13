/**
 * 
 */
package org.sinnlabs.dbvim.ui.annotations;

/**
 * All form events
 * @author peter.liverovsky
 *
 */
public enum EventType {
	NONE,
	FORM_LOADED,
	ENTRY_LOADED,
	CHANGE_FORM_MODE // Raise when user changes form mode (search, modify, ..)
}
