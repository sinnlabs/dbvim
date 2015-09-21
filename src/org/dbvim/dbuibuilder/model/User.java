/**
 * 
 */
package org.dbvim.dbuibuilder.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.dbvim.dbuibuilder.config.ConfigLoader;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Class that represents Account for DBVIM
 * @author peter.liverovsky
 *
 */
@DatabaseTable(tableName = "Accounts")
public class User {

	@DatabaseField(id = true)
	protected String login_name;
	
	@DatabaseField
	protected String fullName;
	
	@DatabaseField(width = 1024, canBeNull = false)
	protected String password_hash;
	
	@DatabaseField(width = 1024, canBeNull = false)
	protected String salt;
	
	@DatabaseField
	protected boolean isEnabled;
	
	public User() {
		// for orm
	}
	
	public User(String login) {
		login_name = login;
	}

	public String getPassword_hash() {
		return password_hash;
	}

	public void setPassword_hash(String password_hash) {
		this.password_hash = password_hash;
	}

	public String getSalt() {
		return salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}

	public String getLogin() {
		return login_name;
	}
	
	public void setEnabled(boolean enabled) {
		isEnabled = enabled;
	}
	
	public boolean isEnabled() { return isEnabled; }
	
	public void setFullName(String fName) { fullName = fName; }
	
	public String getFullName() { return fullName; }
	
	public List<Role> getRoles() throws SQLException {
		QueryBuilder<UserRole, Integer> userRoleQb = 
				ConfigLoader.getInstance().getUserRoles().queryBuilder();
		userRoleQb.selectColumns(UserRole.ROLE_ID_FIELD_NAME);
		
		userRoleQb.where().eq(UserRole.USER_ID_FIELD_NAME, this.login_name);
		
		QueryBuilder<Role, String> roleQb = ConfigLoader.getInstance().getRoles().queryBuilder();
		
		roleQb.where().in(Role.ROLE_NAME_COLUMN, userRoleQb);
		
		List<Role> roles = ConfigLoader.getInstance().getRoles().query(roleQb.prepare());
		
		if (roles == null)
			return new ArrayList<Role>();
		
		return roles;
	}
}
