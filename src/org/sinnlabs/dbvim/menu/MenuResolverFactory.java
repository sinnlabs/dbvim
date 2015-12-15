/**
 * 
 */
package org.sinnlabs.dbvim.menu;

import org.sinnlabs.dbvim.config.ConfigLoader;
import org.sinnlabs.dbvim.model.SearchMenu;
import org.sinnlabs.dbvim.zk.model.IFormComposer;

/**
 * Menu resolver factory. Uses to get instance of menu resolver
 * @author peter.liverovsky
 *
 */
public class MenuResolverFactory {

	/**
	 * Returns Menu resolver instance
	 * @param menuName Menu name to be resolved
	 * @param composer Current form composer
	 * @return MenuResolver instance or null if menu not found
	 * @throws Exception
	 */
	public static MenuResolver getMenuResolver(String menuName, IFormComposer composer) throws Exception {
		// check search menus
		SearchMenu sm = ConfigLoader.getInstance().getSearchMenus().queryForId(menuName);
		if (sm != null) {
			return new SearchMenuResolver(sm, composer);
		}
		return null;
	}
}
