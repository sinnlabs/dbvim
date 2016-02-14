/**
 * 
 */
package org.sinnlabs.dbvim.config;

import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.util.WebAppInit;

/**
 * Class that initializes application
 * @author peter.liverovsky
 *
 */
public class ApplicationInit implements WebAppInit {

	/* (non-Javadoc)
	 * @see org.zkoss.zk.ui.util.WebAppInit#init(org.zkoss.zk.ui.WebApp)
	 */
	@Override
	public void init(WebApp wapp) throws Exception {
		System.out.println("Initializing dbvim configuration.");
		ConfigLoader.initialize(wapp);
	}

}
