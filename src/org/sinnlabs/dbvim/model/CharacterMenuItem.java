/**
 * 
 */
package org.sinnlabs.dbvim.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Class represents character menu item
 * @author peter.liverovsky
 *
 */
@DatabaseTable(tableName = "CharacterMenuItems")
public class CharacterMenuItem {
	
	private final static String MENU_ID_FIELD_NAME = "menu_id";

	@DatabaseField(generatedId = true)
	protected int id;
	
	@DatabaseField
	protected int order;
	
	@DatabaseField(foreign = true, columnName = MENU_ID_FIELD_NAME)
	protected CharacterMenu menu;
	
	@DatabaseField(width = 254)
	protected String label;
	
	@DatabaseField(width = 254)
	protected String value;
	
	/**
	 * Returns menu item id
	 * @return
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Returns the associated menu with this menu item
	 * @return
	 */
	public CharacterMenu getMenu() {
		return menu;
	}
	
	/**
	 * Sets the associated character menu
	 * @param menu
	 */
	public void setMenu(CharacterMenu menu) {
		this.menu = menu;
	}
	
	/**
	 * Returns menu item label
	 * @return
	 */
	public String getLabel() {
		return label;
	}
	
	/**
	 * Returns menu item value
	 * @return
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * Sets menu item label
	 * @param label
	 */
	public void setLabel(String label) {
		this.label = label;
	}
	
	/**
	 * Sets menu item value
	 * @param value
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * Returns menu item order number
	 * @return
	 */
	public int getOrder() {
		return order;
	}
	
	/**
	 * Sets menu item order number
	 * @param order
	 */
	public void setOrder(int order) {
		this.order = order;
	}
}
