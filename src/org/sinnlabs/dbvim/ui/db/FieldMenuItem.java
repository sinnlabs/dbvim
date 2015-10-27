/**
 * 
 */
package org.sinnlabs.dbvim.ui.db;

import org.sinnlabs.dbvim.menu.MenuItem;
import org.zkoss.zul.Menuitem;

/**
 * Class represents Menuitem for field menu
 * @author peter.liverovsky
 *
 */
public class FieldMenuItem extends Menuitem {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6339043773269462770L;
	
	protected MenuItem item = null;

	public FieldMenuItem(MenuItem i) {
		super();
		item = i;
	}
	
	/**
	 * Returns MenuItem contains the value
	 * @return
	 */
	public MenuItem getItem() {
		return item;
	}
}
