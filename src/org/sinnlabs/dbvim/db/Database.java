/**
 * 
 */
package org.sinnlabs.dbvim.db;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.sinnlabs.dbvim.db.exceptions.DatabaseOperationException;
import org.sinnlabs.dbvim.db.model.DBField;
import org.sinnlabs.dbvim.db.model.DBModel;
import org.sinnlabs.dbvim.evaluator.AbstractVariableSet;
import org.sinnlabs.dbvim.evaluator.DatabaseConditionBuilder;
import org.sinnlabs.dbvim.evaluator.exceptions.ParseException;
import org.sinnlabs.dbvim.form.FormFieldResolver;
import org.sinnlabs.dbvim.model.Form;
import org.sinnlabs.dbvim.ui.IField;

/**
 * Class that manages database operations.
 * This includes creating, deleting and checking for the existence of a database.
 * @author peter.liverovsky
 *
 */
public class Database {
	
	/**
	 * Contains all db fields from form
	 */
	protected List<DBField> fields;
	
	/**
	 * Contains all primary id field names
	 */
	protected String[] formIds;
	
	protected FormFieldResolver resolver;
	
	protected Form form;
	
	protected DatabaseConditionBuilder conditionBuilder;
	
	protected Database() {
		
	}
	
	/*package*/ Database(Form form, FormFieldResolver resolver) throws ClassNotFoundException, SQLException, DatabaseOperationException {
		this.form = form;
		DBModel model = new DBModel(form.getDBConnection().getConnectionString(), 
				form.getDBConnection().getClassName());
		fields = model.getFields(form.getCatalog(),
				form.getTableName());
		formIds = findID(form);
		this.resolver = resolver;
		conditionBuilder = new DatabaseConditionBuilder();
	}
	
	/**
	 * Query for all of the rows in the table
	 * @param fields Field list to be selected
	 * @param limit Maximum number of entries to be returned, 0 - means no limit
	 * @return List of entries
	 * @throws DatabaseOperationException 
	 */
	public List<Entry> queryAll(List<IField<?>> fields, int limit) throws DatabaseOperationException {
		try {
			Connection db = DriverManager.getConnection(form.getDBConnection()
					.getConnectionString());

			String[] results = getResultList(fields);

			String sFields = StringUtils.join(ArrayUtils.addAll(formIds, results),
					", ");

			PreparedStatement q = db.prepareStatement("SELECT " + sFields
					+ " FROM " + form.getQualifiedName());

			ResultSet res = q.executeQuery();
			
			List<Entry> entries = readEntries(res, results, limit);
			res.close();
			return entries;
		} catch (SQLException e) {
			System.err.println("ERROR: while executing sql query: "
					+ e.getMessage());
			e.printStackTrace();
			throw new DatabaseOperationException("Error while executing sql query.", e);
		}
	}
	
	/**
	 * Read data from ResultSet
	 * @param res - ResultSet contains query results
	 * @param results - Array of dbfields names. These values are read from the database.
	 * @return list of Entry objects
	 * @throws SQLException 
	 */
	private List<Entry> readEntries(ResultSet res, String[] results, int limit) throws SQLException {
		List<Entry> entries = new ArrayList<Entry>();
		while (res.next()) {
			Entry entry = new Entry();
			// read primary key
			for (int i = 0; i < formIds.length; i++) {
				entry.getID().add(
						getColumnValue(res,
								getFieldByName(fields, formIds[i])));
			}
			// read data
			for (int i = 0; i < results.length; i++) {
				entry.getValues().add(
						getColumnValue(res,
								getFieldByName(fields, results[i])));
			}
			entries.add(entry);
		}
		return entries;
	}
	
	/**
	 * Query rows from the form table width condition
	 * Condition uses like operator if possible, otherwise equal (=)
	 * @param fields List of fields to be selected. 
	 * Can be null, then form result list will be use.
	 * @param condition - List of Values for the condition
	 * @param limit Maximum number of rows to read or 0 if no max specified
	 * @return List of entries
	 * @throws DatabaseOperationException
	 */
	public List<Entry> query(List<IField<?>> fields, List<Value<?>> condition, int limit) throws DatabaseOperationException {
		try {
			Connection db = DriverManager.getConnection(form.getDBConnection()
					.getConnectionString());

			String[] results = getResultList(fields);

			String sFields = StringUtils.join(ArrayUtils.addAll(formIds, results),
					", ");

			String query = "SELECT " + sFields
					+ " FROM " + form.getQualifiedName() + " WHERE ";
			// build condition
			for(int i=0; i<condition.size(); i++) {
				Value<?> v = condition.get(i);
				query += v.getDBField().getName() + " ";
				query += getOperator(v.getDBField()) + " ?";
				if (i<condition.size()-1) {
					query += " AND ";
				}
			}
			
			PreparedStatement ps = db.prepareStatement(query);
			// populate parameters
			setParameters(ps, condition);

			ResultSet res = ps.executeQuery();
			
			List<Entry> entries = readEntries(res, results, limit);
			res.close();
			return entries;
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseOperationException(
					"Error while executing sql query.", e);
		}
	}
	
	/**
	 * Search entries by additional search query
	 * @param fields List of fields to be selected. 
	 * Can be null, then form result list will be use.
	 * @param query - Query string
	 * @param limit Maximum number of rows to read or 0 if no max specified
	 * @param context AbstractVariableSet<Value<?>> that contains special variables for the query
	 * @return List of entries
	 * @throws ParseException 
	 * @throws DatabaseOperationException 
	 */
	public List<Entry> query(List<IField<?>> fields, String query, 
			int limit, AbstractVariableSet<Value<?>> context) throws ParseException, DatabaseOperationException {
		
		List<Value<?>> values = new ArrayList<Value<?>>();
		String dbCondition = conditionBuilder.buildCondition(query, context, resolver, values);
		
		try {
			Connection db = DriverManager.getConnection(form.getDBConnection()
					.getConnectionString());

			String[] results = getResultList(fields);

			String sFields = StringUtils.join(ArrayUtils.addAll(formIds, results),
					", ");

			String dbQuery = "SELECT " + sFields
					+ " FROM " + form.getQualifiedName();
			if (!StringUtils.isBlank(dbCondition)) {
				dbQuery += " WHERE " + dbCondition;
			}
			
			PreparedStatement ps = db.prepareStatement(dbQuery);
			// populate parameters
			setParameters(ps, values);

			ResultSet res = ps.executeQuery();
			
			List<Entry> entries = readEntries(res, results, limit);
			
			res.close();
			return entries;
			
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseOperationException(
					"Error while executing sql query.", e);
		}
	}
	
	/**
	 * Updates all entries in the form
	 * @param values New field values
	 * @throws DatabaseOperationException
	 */
	public void updateAll(List<Value<?>> values) throws DatabaseOperationException {
		// Connect to the db
		try {
			Connection db = DriverManager.getConnection(form.getDBConnection()
					.getConnectionString());

			// build update query
			String query = "UPDATE " + form.getQualifiedName()
					+ " SET ";
			// add values to the query
			for (int i=0; i<values.size(); i++) {
				query += values.get(i).getDBField().getName() + " = ?";
				if (i<values.size()-1)
					query += ", ";
				else
					query += " ";
			}

			// Prepare query
			PreparedStatement ps = db.prepareStatement(query);

			// setup all query parameters
			// set values to update
			for(int i=0; i<values.size(); i++) {
				setParameter(ps, i+1, values.get(i));
			}
			
			// Update entry:
			ps.executeUpdate();
		} catch (SQLException e1) {
			System.err.println("ERROR: Unable to update entry: ");
			e1.printStackTrace();
			throw new DatabaseOperationException("Unable to update entries.", e1);
		}
	}
	
	/**
	 * Updates multiple records
	 * @param condition Qualification
	 * @param values New field values
	 * @throws DatabaseOperationException 
	 */
	public void update(List<Value<?>> condition, List<Value<?>> values) throws DatabaseOperationException {
		// Connect to the db
		try {
			Connection db = DriverManager.getConnection(form.getDBConnection()
					.getConnectionString());

			// build update query
			String query = "UPDATE " + form.getQualifiedName()
					+ " SET ";
			// add values to the query
			for (int i=0; i<values.size(); i++) {
				query += values.get(i).getDBField().getName() + " = ?";
				if (i<values.size()-1)
					query += ", ";
				else
					query += " ";
			}

			// build query qualification
			query += " WHERE ";
			// build condition
			for(int i=0; i<condition.size(); i++) {
				Value<?> v = condition.get(i);
				query += v.getDBField().getName() + " ";
				query += getOperator(v.getDBField()) + " ?";
				if (i<condition.size()-1) {
					query += " AND ";
				}
			}

			// Prepare query
			PreparedStatement ps = db.prepareStatement(query);

			// setup all query parameters
			// set values to update
			for(int i=0; i<values.size(); i++) {
				setParameter(ps, i+1, values.get(i));
			}
			// set qualification
			for(int i=0; i<condition.size(); i++) {
				setParameter(ps, values.size()+i+1, condition.get(i));
			}

			// Update entry:
			ps.executeUpdate();
		} catch (SQLException e1) {
			System.err.println("ERROR: Unable to update entry: ");
			e1.printStackTrace();
			throw new DatabaseOperationException("Unable to update entry.", e1);
		}
	}
	
	/**
	 * Updates all matching requests
	 * @param values List of new values
	 * @param query qualification
	 * @param context qualification context
	 * @throws ParseException
	 * @throws DatabaseOperationException
	 */
	public void update(List<Value<?>> values, String query, 
			AbstractVariableSet<Value<?>> context) throws ParseException, DatabaseOperationException {
		List<Value<?>> condition = new ArrayList<Value<?>>();
		String dbCondition = conditionBuilder.buildCondition(query, context, resolver, condition);
		
		try {
			Connection db = DriverManager.getConnection(form.getDBConnection()
					.getConnectionString());

			// build update query
			String dbQuery = "UPDATE " + form.getQualifiedName()
					+ " SET ";
			// add values to the query
			for (int i=0; i<values.size(); i++) {
				dbQuery += values.get(i).getDBField().getName() + " = ?";
				if (i<values.size()-1)
					dbQuery += ", ";
				else
					dbQuery += " ";
			}

			if (!StringUtils.isBlank(dbCondition)) {
				dbQuery += " WHERE " + dbCondition;
			}
			
			
			PreparedStatement ps = db.prepareStatement(dbQuery);
			
			// setup all query parameters
			// set values to update
			for(int i=0; i<values.size(); i++) {
				setParameter(ps, i+1, values.get(i));
			}
			// set qualification
			for(int i=0; i<condition.size(); i++) {
				setParameter(ps, values.size()+i+1, condition.get(i));
			}

			// Update entry:
			ps.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseOperationException(
					"Error while executing sql query.", e);
		}

	}

	/**
	 * Returns operator string
	 * @param field
	 * @return
	 */
	protected static String getOperator(DBField field) {
		switch(field.getDBType()) {
		case java.sql.Types.CHAR:
		case java.sql.Types.VARCHAR:
		case java.sql.Types.LONGVARCHAR:
		case java.sql.Types.NCHAR:
		case java.sql.Types.NVARCHAR:
		case java.sql.Types.LONGNVARCHAR:
			return "LIKE";
		case java.sql.Types.TINYINT:
		case java.sql.Types.SMALLINT:
		case java.sql.Types.INTEGER:
		case java.sql.Types.DECIMAL:
		case java.sql.Types.NUMERIC:
		case java.sql.Types.REAL:
		case java.sql.Types.FLOAT:
		case java.sql.Types.DOUBLE:
		case java.sql.Types.BIGINT:
			return "=";
		}
		return "LIKE";
	}

	/**
	 * Reads all entry values
	 * @param e - Entry contains valid IDs values
	 * @param form - form Object
	 * @return Entry contains all fields values or null if entry does not exists
	 * @throws DatabaseOperationException 
	 */
	public Entry readEntry(Entry e) throws DatabaseOperationException {
		try {
			// connect to the db
			Connection db = DriverManager.getConnection(form.getDBConnection()
					.getConnectionString());

			// prepare sql query
			String query = "SELECT * FROM " + form.getQualifiedName();
			query += " WHERE ";
			
			// build qualification
			for(int i=0; i<e.getID().size(); i++) {
				query += e.getID().get(i).getDBField().getName();
				query += " = ?";
				if (i<e.getID().size()-1) {
					query += " AND ";
				}
			}
			
			// Prepare query
			PreparedStatement ps = db.prepareStatement(query);
			// populate parameters
			setParameters(ps, e);
			
			ResultSet set = ps.executeQuery();
			// if no found
			if (!set.next()) 
				return null;
			
			// build entry object
			Entry result = new Entry();
			for(DBField cf : fields) {
				Value<?> v = getColumnValue(set, cf);
				if (cf.isPrimaryKey())
					result.getID().add(v);
				result.getValues().add(v);
			}
			return result;
		} catch (SQLException e1) {
			e1.printStackTrace();
			throw new DatabaseOperationException("Error while executing sql query.", e1);
		}
	}
	
	/**
	 * Updated the existing entry
	 * @param e - Fully completed entry. See {@link #readEntry(Entry)}
	 * @param values - new entry values
	 * @throws DatabaseOperationException
	 */
	public void updateEntry(Entry e, List<Value<?>> values) throws DatabaseOperationException {
		// find updated values
		List<Value<?>> newValues = new ArrayList<Value<?>>();
		for(Value<?> nv : values) {
			for(Value<?> ov : e.getValues()) {
				// if new value is different
				if (nv.getDBField().getName().equals(ov.getDBField().getName())
						&& (ov.getValue() == null || !nv.getValue().equals(ov.getValue())) ) {
					// add new value to the list
					newValues.add(nv);
				}
			}
		}
		
		// if no value updated, return
		if (newValues.size() == 0)
			return;
		
		// Connect to the db
		try {
			Connection db = DriverManager.getConnection(form.getDBConnection()
					.getConnectionString());
		
			// build update query
			String query = "UPDATE " + form.getQualifiedName()
					+ " SET ";
			// add values to the query
			for (int i=0; i<newValues.size(); i++) {
				query += newValues.get(i).getDBField().getName() + " = ?";
				if (i<newValues.size()-1)
					query += ", ";
				else
					query += " ";
			}
			
			// build query qualification
			query += " WHERE ";
			for(int i=0; i<e.getID().size(); i++) {
				query += e.getID().get(i).getDBField().getName();
				query += " = ?";
				if (i<e.getID().size()-1) {
					query += " AND ";
				}
			}
			
			// Prepare query
			PreparedStatement ps = db.prepareStatement(query);
			
			// setup all query parameters
			// set values to update
			for(int i=0; i<newValues.size(); i++) {
				setParameter(ps, i+1, newValues.get(i));
			}
			// set qualification
			for(int i=0; i<e.getID().size(); i++) {
				setParameter(ps, newValues.size()+i+1, e.getID().get(i));
			}
			
			// Update entry:
			ps.executeUpdate();
		} catch (SQLException e1) {
			System.err.println("ERROR: Unable to update entry: ");
			e1.printStackTrace();
			throw new DatabaseOperationException("Unable to update entry.", e1);
		}
	}
	
	/**
	 * Inserts new entry
	 * @param e - Entry with filled values
	 * @throws DatabaseOperationException
	 */
	public void insertEntry(Entry e) throws DatabaseOperationException {
		// build query
		String query = "INSERT INTO " + form.getQualifiedName() + " (";
		
		for (int i=0; i<e.getValues().size(); i++) {
			query += e.getValues().get(i).getDBField().getName();
			if (i<e.getValues().size()-1)
				query += ", ";
		}
		query += ") VALUES (";
		for (int i=0; i<e.getValues().size(); i++) {
			query += "?";
			if (i<e.getValues().size()-1)
				query += ", ";
		}
		query += ")";
		
		try {
			// connect to the db
			Connection db = DriverManager.getConnection(form.getDBConnection()
					.getConnectionString());
			
			// prepare statement
			PreparedStatement ps = db.prepareStatement(query);
			
			// set values
			for(int i=0; i<e.getValues().size(); i++) {
				setParameter(ps, i+1, e.getValues().get(i));
			}
			
			ps.executeUpdate();
		} catch (SQLException e1) {
			e1.printStackTrace();
			throw new DatabaseOperationException("Unable to create entry.", e1);
		}
	}
	
	/**
	 * Delete entry
	 * @param e - Entry with correct ID
	 * @throws DatabaseOperationException
	 */
	public void deleteEntry(Entry e) throws DatabaseOperationException {
		// build query
		String query = "DELETE FROM " + form.getQualifiedName() + " WHERE ";
		
		// build qualification
		for(int i=0; i<e.getID().size(); i++) {
			query += e.getID().get(i).getDBField().getName();
			query += " = ?";
			if (i<e.getID().size()-1)
				query += " AND ";
		}

		try {
			// connect to the db
			Connection db;
			db = DriverManager.getConnection(form.getDBConnection()
					.getConnectionString());
			
			PreparedStatement ps = db.prepareStatement(query);
			// set qualification values
			setParameters(ps, e);
			
			ps.executeUpdate();
			
		} catch (SQLException e1) {
			System.err.println("ERROR: Unable to delete entry: " + e1.getMessage());
			e1.printStackTrace();
			throw new DatabaseOperationException("Unable to delete entry: " + e1.getMessage(), e1);
		}
	}

	/**
	 * Find all primary id.
	 * @param form
	 * @return Array of strings, contains primary id column names
	 */
	public static String[] findID(Form form) {
		List<String> res = new ArrayList<String>();
		try {

			DBModel model = new DBModel(form.getDBConnection()
					.getConnectionString(), form.getDBConnection().getClassName());

			List<DBField> fields = model.getFields(form.getCatalog(),
					form.getTableName());

			for (DBField field : fields) {
				if (field.isPrimaryKey())
					res.add(field.getName());
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return (String[]) res.toArray(new String[res.size()]);
	}

	private static DBField getFieldByName(List<DBField> fields, String name) {
		if (fields == null)
			return null;

		for (DBField field : fields) {
			if (field.getName().equals(name))
				return field;
		}
		return null;
	}

	private String[] getResultList(List<IField<?>> fields) {
		if (fields == null) {
			String[] res = new String[form.getResultList().size()];
			for (int i=0; i<res.length; i++) {
				res[i] = resolver.getFields().get(
						form.getResultList().get(i).fieldName).getDBField().getName();
			}
			return res;
		}
		String[] res = new String[fields.size()];
		for (int i=0; i<res.length; i++) {
			res[i] = fields.get(i).getDBField().getName();
		}
		return res;
	}
	
	/**
	 * Reads column value
	 * @param res - ResultSet contains data
	 * @param field - DBField
	 * @return Value<?> object
	 * @throws SQLException
	 */
	public static Value<?> getColumnValue(ResultSet res, DBField field)
			throws SQLException {
		return getColumnValue(res, field, field.getName());
	}
	
	/**
	 * Get the value for the column
	 * @param res ResultSet contains query results
	 * @param field DBField describes column value
	 * @param alias alias for the column in the query (select ID field1, ...)
	 * @return Value<?> for the column
	 * @throws SQLException
	 */
	protected static Value<?> getColumnValue(ResultSet res, DBField field, String alias) throws SQLException {
		switch (field.getDBType()) {
		case java.sql.Types.CHAR:
		case java.sql.Types.VARCHAR:
		case java.sql.Types.LONGVARCHAR:
			Value<String> val = new Value<String>(
					res.getString(alias), field);
			return val;
		case java.sql.Types.NCHAR:
		case java.sql.Types.NVARCHAR:
		case java.sql.Types.LONGNVARCHAR:
			Value<String> nval = new Value<String>(
					res.getNString(alias), field);
			return nval;
		case java.sql.Types.TINYINT:
		case java.sql.Types.SMALLINT:
		case java.sql.Types.INTEGER:
			Integer ival = res.getInt(alias);
			if (res.wasNull())
				return new Value<Integer>(null, field);
			return new Value<Integer>(ival, field);
		case java.sql.Types.DECIMAL:
		case java.sql.Types.NUMERIC:
			return new Value<BigDecimal>(res.getBigDecimal(alias),
					field);
		case java.sql.Types.REAL:
		case java.sql.Types.FLOAT:
		case java.sql.Types.DOUBLE:
			Double dval = res.getDouble(alias);
			if (res.wasNull())
				return new Value<Double>(null, field);
			return new Value<Double>(dval, field);
		case java.sql.Types.BIGINT:
			Long lval = res.getLong(alias);
			if (res.wasNull())
				return new Value<Long>(null, field);
			return new Value<Long>(lval, field);
		case java.sql.Types.DATE:
			return new Value<Date>(res.getDate(alias), field);
		case java.sql.Types.TIME:
			return new Value<Time>(res.getTime(alias), field);
		case java.sql.Types.TIMESTAMP:
			return new Value<Timestamp>(res.getTimestamp(alias), field);
		default:
			return new Value<Object>(res.getObject(alias), field);
		}
	}
	
	/**
	 * Set PreparedStatement parameters by Entry ID
	 * @param ps PreparedStatement with parameters ('?')
	 * @param e Entry with valid IDs.
	 * @throws SQLException
	 */
	protected static void setParameters(PreparedStatement ps, Entry e) throws SQLException {
		for(int i=0; i<e.getID().size(); i++) {
			setParameter(ps, i+1, e.getID().get(i));
		}
	}
	
	/**
	 * Set PreparedStatement parameters
	 * @param ps PreparedStatement with parameters
	 * @param values Value<?> list sorted in the order of the parameters 
	 * @throws SQLException
	 */
	protected static void setParameters(PreparedStatement ps, List<Value<?>> values) 
			throws SQLException {
		for(int i=0; i<values.size(); i++) {
			setParameter(ps, i+1, values.get(i));
		}
	}
	
	@SuppressWarnings("unchecked")
	protected static void setParameter(PreparedStatement ps, int id, Value<?> v) throws SQLException {
		switch(v.getDBField().getDBType()) {
		case java.sql.Types.CHAR:
		case java.sql.Types.VARCHAR:
		case java.sql.Types.LONGVARCHAR:
			Value<String> str = (Value<String>) v;
			if (str.getValue() != null)
				ps.setString(id, str.getValue());
			else
				ps.setNull(id, v.getDBField().getDBType());
			break;
		case java.sql.Types.NCHAR:
		case java.sql.Types.NVARCHAR:
		case java.sql.Types.LONGNVARCHAR:
			Value<String> nstr = (Value<String>) v;
			if (nstr.getValue() != null)
				ps.setNString(id, nstr.getValue());
			else
				ps.setNull(id, v.getDBField().getDBType());
			break;
		case java.sql.Types.TINYINT:
		case java.sql.Types.SMALLINT:
		case java.sql.Types.INTEGER:
			Value<Integer> i = (Value<Integer>) v;
			if (i.getValue() != null)
				ps.setInt(id, i.getValue());
			else
				ps.setNull(id, i.getDBField().getDBType());
			break;
		case java.sql.Types.DECIMAL:
		case java.sql.Types.NUMERIC:
			Value<BigDecimal> dec = (Value<BigDecimal>) v;
			if (dec.getValue() != null)
				ps.setBigDecimal(id, dec.getValue());
			else
				ps.setNull(id, v.getDBField().getDBType());
			break;
		case java.sql.Types.REAL:
		case java.sql.Types.FLOAT:
		case java.sql.Types.DOUBLE:
			Value<Double> dob = (Value<Double>) v;
			if (dob.getValue() != null)
				ps.setDouble(id, dob.getValue());
			else
				ps.setNull(id, dob.getDBField().getDBType());
			break;
		case java.sql.Types.BIGINT:
			Value<Long> lon = (Value<Long>) v;
			if (lon.getValue() != null)
				ps.setLong(id, lon.getValue());
			else
				ps.setNull(id, lon.getDBField().getDBType());
			break;
		case java.sql.Types.DATE:
			Value<Date> date = (Value<Date>) v;
			if (date.getValue() != null)
				ps.setDate(id, (java.sql.Date) date.getValue());
			else
				ps.setNull(id, date.getDBField().getDBType());
			break;
		case java.sql.Types.TIME:
			Value<Time> time = (Value<Time>) v;
			if (time.getValue() != null)
				ps.setTime(id, time.getValue());
			else
				ps.setNull(id, time.getDBField().getDBType());
			break;
		case java.sql.Types.TIMESTAMP:
			Value<Timestamp> ts = (Value<Timestamp>) v;
			if (ts.getValue() != null)
				ps.setTimestamp(id, ts.getValue());
			else
				ps.setNull(id, ts.getDBField().getDBType());
			break;
		}
	}
}