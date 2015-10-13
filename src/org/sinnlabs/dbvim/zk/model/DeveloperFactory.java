/**
 * 
 */
package org.sinnlabs.dbvim.zk.model;

import org.zkoss.zk.ui.Executions;

/**
 * Class is used to get IDeveloperStudio instance
 * @author peter.liverovsky
 *
 */
public class DeveloperFactory<T extends IDeveloperStudio> {
	
	public static IDeveloperStudio getInstance() {
		return  (IDeveloperStudio) Executions.getCurrent().getAttribute("composer");
	}
}
