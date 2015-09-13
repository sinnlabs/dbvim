/**
 * 
 */
package org.dbvim.dbuibuilder.config;

import java.sql.SQLException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.dbvim.dbuibuilder.model.DBConnection;
import org.dbvim.dbuibuilder.model.Form;
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
	
	protected Dao<DBConnection, String> dbConnectionDao;
	
	protected Dao<Form, String> forms;
	
	private static volatile ConfigLoader instance;

	private ConfigLoader() {
		WebApp webApp = Executions.getCurrent().getDesktop().getWebApp();
		// get the real path of the configuration file
		String uri = webApp.getRealPath("/WEB-INF/config.xml");
		loadConfig(uri);
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
	
	public String getJDBCString() {
		return jdbcString;
	}
	
	public Dao<DBConnection, String> getDBConnections() {
		return dbConnectionDao;
	}
	
	public Dao<Form, String> getForms() {
		return forms;
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
	
	protected void initializeDB() throws SQLException {
		connectionSource = new JdbcPooledConnectionSource(jdbcString);
		
		dbConnectionDao = DaoManager.createDao(connectionSource, DBConnection.class);
		if (!dbConnectionDao.isTableExists()) {
			TableUtils.createTableIfNotExists(connectionSource, DBConnection.class);
		}
		
		forms = DaoManager.createDao(connectionSource, Form.class);
		if (!forms.isTableExists()) {
			TableUtils.createTableIfNotExists(connectionSource, Form.class);
		}
	}
}
