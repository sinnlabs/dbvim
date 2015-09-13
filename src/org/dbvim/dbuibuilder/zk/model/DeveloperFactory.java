/**
 * 
 */
package org.dbvim.dbuibuilder.zk.model;

import org.zkoss.zk.ui.Sessions;

/**
 * Class is used to get IDeveloperStudio instance
 * @author peter.liverovsky
 *
 */
public class DeveloperFactory<T extends IDeveloperStudio> {
	
	public static IDeveloperStudio getInstance() {
		return (IDeveloperStudio) Sessions.getCurrent().getAttribute("DEVELOPER");
	}
}
