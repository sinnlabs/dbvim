/**
 * 
 */
package org.sinnlabs.dbvim.menu;

import java.util.List;

/**
 * Menu resolver interface
 * @author peter.liverovsky
 *
 */
public interface MenuResolver {

	/**
	 * Returns menu items
	 * @return
	 * @throws Exception
	 */
	public List<MenuItem> getItems() throws Exception;
	
	/**
	 * Gets MenuItem by label
	 * @param label Label to be searched
	 * @return MenuItem
	 * @throws Exception
	 */
	public MenuItem byLabel(Object label) throws Exception;
	
	/**
	 * Gets MenuItem by Value
	 * @param value Value to be searched
	 * @return MenuItem
	 * @throws Exception
	 */
	public MenuItem byValue(Object value) throws Exception;
}
