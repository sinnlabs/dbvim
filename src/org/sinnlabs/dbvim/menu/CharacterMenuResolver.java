/**
 * 
 */
package org.sinnlabs.dbvim.menu;

import java.util.ArrayList;
import java.util.List;

import org.sinnlabs.dbvim.model.CharacterMenu;
import org.sinnlabs.dbvim.model.CharacterMenuItem;

/**
 * @author peter.liverovsky
 *
 */
public class CharacterMenuResolver implements MenuResolver {
	
	private CharacterMenu menu;
	
	public CharacterMenuResolver(CharacterMenu menu) {
		this.menu = menu;
	}
	
	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.menu.MenuResolver#getItems()
	 */
	@Override
	public List<MenuItem> getItems() throws Exception {
		List<MenuItem> items = new ArrayList<MenuItem>();
		List<CharacterMenuItem> sorted = new ArrayList<CharacterMenuItem>();
		
		/** Sort menu items by order **/
		for(CharacterMenuItem i : menu.getItems()) {
			boolean added = false;
			for(int k=0; k<sorted.size(); k++) {
				if (i.getOrder() < sorted.get(k).getOrder()) {
					sorted.add(k, i);
					added = true;
				}
			}
			if (!added)
				sorted.add(i);
		}
		
		/** Convert CharacterMenu to MenuItem list **/
		for(CharacterMenuItem i : sorted) {
			MenuItem item = new MenuItem();
			item.setLabel(i.getLabel());
			item.setValue(i.getValue());
			items.add(item);
		}
		return items;
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.menu.MenuResolver#byLabel(java.lang.Object)
	 */
	@Override
	public MenuItem byLabel(Object label) throws Exception {
		List<MenuItem> menuItems = this.getItems();
		
		for (MenuItem i : menuItems) {
			if (i.getLabel().equals(label)) {
				return i;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.menu.MenuResolver#byValue(java.lang.Object)
	 */
	@Override
	public MenuItem byValue(Object value) throws Exception {
		List<MenuItem> menuItems = this.getItems();
		
		for (MenuItem i : menuItems) {
			if (i.getValue().equals(value)) {
				return i;
			}
		}
		return null;
	}

}
