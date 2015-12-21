/**
 * 
 */
package org.sinnlabs.dbvim.model;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Class represents character menu
 * @author peter.liverovsky
 *
 */
@DatabaseTable(tableName = "CharacterMenus")
public class CharacterMenu {

	public static final String NAME_FIELD_NAME = "name";
	
	@DatabaseField(id = true, columnName = NAME_FIELD_NAME)
	protected String name;
	
	@ForeignCollectionField(eager = true)
    ForeignCollection<CharacterMenuItem> items;
	
	/**
	 * Returns CharacterMenu name
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets CharacterMenu name
	 * @param name Menu name
	 */
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Returns CharacterMenu items
	 * @return
	 */
	public ForeignCollection<CharacterMenuItem> getItems() {
		return items;
	}
}
