/**
 * 
 */
package org.dbvim.dbuibuilder.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Class represents relations between Users and Roles
 * @author peter.liverovsky
 *
 */
@DatabaseTable(tableName = "UserRolesRelations")
public class UserRole {

	public final static String USER_ID_FIELD_NAME = "user_id";
	public final static String ROLE_ID_FIELD_NAME = "role_id";

	/**
	 * This id is generated by the database and set on the object when it is passed to the create method. An id is
	 * needed in case we need to update or delete this object in the future.
	 */
	@DatabaseField(generatedId = true)
	int id;

	// This is a foreign object which just stores the id from the User object in this table.
	@DatabaseField(foreign = true, columnName = USER_ID_FIELD_NAME)
	User user;

	// This is a foreign object which just stores the id from the Post object in this table.
	@DatabaseField(foreign = true, columnName = ROLE_ID_FIELD_NAME)
	Role role;
	
	public UserRole() {
		// for orm
	}
	
	public UserRole(User user, Role role) {
		this.user = user;
		this.role = role;
	}
}