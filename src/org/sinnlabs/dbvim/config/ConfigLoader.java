/**
 * 
 */
package org.sinnlabs.dbvim.config;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import javax.servlet.ServletContext;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.sinnlabs.dbvim.model.DBConnection;
import org.sinnlabs.dbvim.model.Form;
import org.sinnlabs.dbvim.model.Role;
import org.sinnlabs.dbvim.model.SearchMenu;
import org.sinnlabs.dbvim.model.StaticResource;
import org.sinnlabs.dbvim.model.User;
import org.sinnlabs.dbvim.model.UserRole;
import org.sinnlabs.dbvim.rules.engine.Rules;
import org.sinnlabs.dbvim.rules.engine.RulesEngine;
import org.sinnlabs.dbvim.security.LoginProvider;
import org.springframework.context.ApplicationContext;
import org.zkoss.idom.Element;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WebApp;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.jdbc.JdbcPooledConnectionSource;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

/**
 * @author peter.liverovsky
 *
 */
public class ConfigLoader {
	
	protected String jdbcString;
	
	protected ConnectionSource connectionSource;
	
	protected Rules rules;
	
	protected Dao<DBConnection, String> dbConnectionDao;
	
	protected Dao<Form, String> forms;
	
	protected Dao<User, String> users;
	
	protected Dao<Role, String> roles;
	
	protected Dao<UserRole, Integer> userroles;
	
	protected Dao<SearchMenu, String> searchMenus;
	
	protected Dao<StaticResource, String> staticResources;
	
	private static volatile ConfigLoader instance;

	private ConfigLoader() {
		WebApp webApp = Executions.getCurrent().getDesktop().getWebApp();
		
		// get the real path of the configuration file
		String uri = webApp.getRealPath("/config/rules/rules.xml");

		if (StringUtils.isEmpty(uri))
			return;

		// load the rules using the rules engine
		rules = RulesEngine.loadComponentRules(uri);
		
		// get the real path of the configuration file
		uri = webApp.getRealPath("/WEB-INF/config.xml");
		
		loadConfig(uri);
		try {
			initializeDB();
		} catch (SQLException e) {
			System.err.println("Unable to initialize db: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private ConfigLoader(ApplicationContext context) throws IOException {
		String config = context.getResource("/WEB-INF/config.xml").getFile().getAbsolutePath();
		
		loadConfig(config);
		
		String rulesPath = context.getResource("/config/rules/rules.xml").getFile().getAbsolutePath();
		// load the rules using the rules engine
		rules = RulesEngine.loadComponentRules(rulesPath);
				
		try {
			initializeDB();
		} catch (SQLException e) {
			System.err.println("Unable to initialize db: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	private ConfigLoader(ServletContext context) throws IOException {
		String config = context.getRealPath("/WEB-INF/config.xml");

		loadConfig(config);
		
		String rulesPath = context.getRealPath("/config/rules/rules.xml");
		// load the rules using the rules engine
		rules = RulesEngine.loadComponentRules(rulesPath);
				
		try {
			initializeDB();
		} catch (SQLException e) {
			System.err.println("Unable to initialize db: " + e.getMessage());
			e.printStackTrace();
		}
	}

	/**
	 * Get the instance
	 * 
	 * @return
	 */
	public static ConfigLoader getInstance() {
		ConfigLoader result = instance;
		/* First check */
		if (result == null) {
			synchronized (ConfigLoader.class) {
				result = instance;
				/* second check with locking */
				if (result == null)
					instance = result = new ConfigLoader();
			}
		}
		return result;
	}
	
	public static void initialize(ApplicationContext context) throws IOException {
		ConfigLoader result = instance;
		/* First check */
		if (result == null) {
			synchronized (ConfigLoader.class) {
				result = instance;
				/* second check with locking */
				if (result == null)
					instance = result = new ConfigLoader(context);
			}
		}
	}
	
	public static void initialize(ServletContext context) throws IOException {
		ConfigLoader result = instance;
		/* First check */
		if (result == null) {
			synchronized (ConfigLoader.class) {
				result = instance;
				/* second check with locking */
				if (result == null)
					instance = result = new ConfigLoader(context);
			}
		}
	}
	
	public String getJDBCString() {
		return jdbcString;
	}
	
	public Dao<DBConnection, String> getDBConnections() {
		return dbConnectionDao;
	}
	
	public Dao<Form, String> getForms() {
		return forms;
	}
	
	public Dao<User, String> getUsers() {
		return users;
	}
	
	public Dao<UserRole, Integer> getUserRoles() {
		return userroles;
	}
	
	public Dao<Role, String> getRoles() {
		return roles;
	}
	
	public Dao<SearchMenu, String> getSearchMenus() {
		return searchMenus;
	}
	
	public Dao<StaticResource, String> getStaticResources() {
		return staticResources;
	}
	
	/**
	 * Returns the Rules object that contains all
	 * active component rules
	 * 
	 */
	public Rules getRules() {
		return rules;
	}

	protected void loadConfig(String uri) {
		if (StringUtils.isEmpty(uri))
			return;

		Configurator config = null;

		try {
			// load the configuration file
			config = new Configurator(uri);

			// get all the rules entries directly from the
			// iDOM document
			Element[] arrConnections = config.getElements("connection", null);

			if (ArrayUtils.isEmpty(arrConnections))
				return;
			
			// use only first connection entry
			jdbcString = arrConnections[0].getAttribute("JDBC");

		} catch (Exception e) {
			return;
		} finally {
			config = null;
		}
	}
	
	/**
	 * Initialize DAO objects
	 * @throws SQLException
	 */
	protected synchronized void initializeDB() throws SQLException {
		connectionSource = new JdbcPooledConnectionSource(jdbcString);
		
		dbConnectionDao = DaoManager.createDao(connectionSource, DBConnection.class);
		if (!dbConnectionDao.isTableExists()) {
			TableUtils.createTableIfNotExists(connectionSource, DBConnection.class);
		}
		
		forms = DaoManager.createDao(connectionSource, Form.class);
		if (!forms.isTableExists()) {
			TableUtils.createTableIfNotExists(connectionSource, Form.class);
		}
		
		roles = DaoManager.createDao(connectionSource, Role.class);
		if (!roles.isTableExists()) {
			TableUtils.createTableIfNotExists(connectionSource, Role.class);
			// create default roles
			roles.create(new Role(Role.ROLE_USER, "Has access to all data forms"));
			roles.create(new Role(Role.ROLE_ADMIN, "Full access."));
		}
		
		users = DaoManager.createDao(connectionSource, User.class);
		if (!users.isTableExists()) {
			TableUtils.createTableIfNotExists(connectionSource, User.class);
			// create default users
			try {
				users.create(LoginProvider.createUser("user", "user"));
				users.create(LoginProvider.createUser("admin", "admin"));
			} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
				System.err.println("ERROR: Unable to initialize default users.");
				e.printStackTrace();
			}
		}
		
		userroles = DaoManager.createDao(connectionSource, UserRole.class);
		if (!userroles.isTableExists()) {
			TableUtils.createTableIfNotExists(connectionSource, UserRole.class);
			// create default relations
			User user = users.queryForId("user");
			User admin = users.queryForId("admin");
			Role userRole = roles.queryForId(Role.ROLE_USER);
			Role adminRole = roles.queryForId(Role.ROLE_ADMIN);
			userroles.create(new UserRole(user, userRole));
			userroles.create(new UserRole(admin, adminRole));
		}
		
		searchMenus = DaoManager.createDao(connectionSource, SearchMenu.class);
		if (!searchMenus.isTableExists()) {
			TableUtils.createTableIfNotExists(connectionSource, SearchMenu.class);
		}
		
		staticResources = DaoManager.createDao(connectionSource, StaticResource.class);
		if (!staticResources.isTableExists()) {
			TableUtils.createTableIfNotExists(connectionSource, StaticResource.class);
		}
	}
}
