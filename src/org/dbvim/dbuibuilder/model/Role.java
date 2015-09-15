/**
 * 
 */
package org.dbvim.dbuibuilder.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Class represents role object
 * @author peter.liverovsky
 *
 */
@DatabaseTable(tableName = "Roles")
public class Role {
	
	public static final String ROLE_USER = "ROLE_USER";
	
	public static final String ROLE_ADMIN = "ROLE_ADMIN";
	
	public static final String ROLE_NAME_COLUMN = "role_name";
	
	@DatabaseField(id = true, columnName = ROLE_NAME_COLUMN)
	protected String role_name;
	
	public Role() {
		
	}
	
	public Role(String name) {
		role_name = name;
	}
	
	public String getName() { return role_name; }
}
