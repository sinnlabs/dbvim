/**
 * 
 */
package org.dbvim.dbuibuilder.zk.model;

import org.zkoss.zk.ui.Sessions;

/**
 * @author peter.liverovsky
 *
 */
public class CurrentForm {
	public static ICurrentForm getInstance() {
		return (ICurrentForm) Sessions.getCurrent().getAttribute("CURRENTFORM");
	}
}
