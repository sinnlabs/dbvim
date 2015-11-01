/**
 * 
 */
package org.sinnlabs.dbvim.db;

import java.sql.SQLException;

import org.sinnlabs.dbvim.db.exceptions.DatabaseOperationException;
import org.sinnlabs.dbvim.form.FormFieldResolver;
import org.sinnlabs.dbvim.model.Form;

/**
 * @author peter.liverovsky
 *
 */
public class DatabaseFactory {
	
	public static Database createInstance(Form f, FormFieldResolver r) 
			throws ClassNotFoundException, DatabaseOperationException, SQLException {
		if (f.isJoin()) {
			return new DatabaseJoin(f, r);
		}
		return new Database(f, r);
	}
}
