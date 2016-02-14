/**
 * 
 */
package org.sinnlabs.dbvim.config;

import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.util.WebAppCleanup;

/**
 * Cleanup resources
 * 
 * @author peter.liverovsky
 *
 */
public class ApplicationCleanup implements WebAppCleanup {

	@Override
	public void cleanup(WebApp wapp) throws Exception {
		System.out.println("Clean up resources.");
		ConfigLoader.getInstance().dispose();
	}

}
