/**
 * 
 */
package org.sinnlabs.dbvim.model;

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
	
	@DatabaseField(width=256)
	protected String roleDescription;
	
	public Role() {
		
	}
	
	public Role(String name) {
		role_name = name;
	}
	
	public Role(String name, String desc) {
		this(name);
		roleDescription = desc;
	}
	
	public String getName() { return role_name; }
	
	public String getDescription() { return roleDescription; }
	
	public void setDescription(String desc) { roleDescription = desc; }
}
