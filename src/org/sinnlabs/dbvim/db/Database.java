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
import org.sinnlabs.dbvim.model.Form;

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
	
	protected Form form;
	
	public Database(Form form) throws ClassNotFoundException, SQLException {
		this.form = form;
		DBModel model = new DBModel(form.getDBConnection().getConnectionString(), 
				form.getDBConnection().getClassName());
		fields = model.getFields(form.getCatalog(),
				form.getTableName());
		formIds = findID(form);
	}
	
	/**
	 * Query for all of the rows in the table
	 * @return List of entries
	 * @throws DatabaseOperationException 
	 */
	public List<Entry> queryAll() throws DatabaseOperationException {
		try {
			Connection db = DriverManager.getConnection(form.getDBConnection()
					.getConnectionString());

			String[] results = form.getResultList();

			String fields = StringUtils.join(ArrayUtils.addAll(formIds, results),
					", ");

			PreparedStatement q = db.prepareStatement("select " + fields
					+ " from " + form.getQualifiedName());

			ResultSet res = q.executeQuery();
			
			return readEntries(res, results);
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
	private List<Entry> readEntries(ResultSet res, String[] results) throws SQLException {
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
	 * @param condition - List of Values for the condition
	 * @return List of entries
	 * @throws DatabaseOperationException
	 */
	public List<Entry> query(List<Value<?>> condition) throws DatabaseOperationException {
		try {
			Connection db = DriverManager.getConnection(form.getDBConnection()
					.getConnectionString());

			String[] results = form.getResultList();

			String fields = StringUtils.join(ArrayUtils.addAll(formIds, results),
					", ");

			String query = "select " + fields
					+ " from " + form.getQualifiedName() + " where ";
			// build condition
			for(int i=0; i<condition.size(); i++) {
				Value<?> v = condition.get(i);
				query += v.getDBField().getName() + " ";
				query += getOperator(v.getDBField()) + " ?";
				if (i<condition.size()-1) {
					query += " and ";
				}
			}
			
			PreparedStatement ps = db.prepareStatement(query);
			// populate parameters
			setParameters(ps, condition);

			ResultSet res = ps.executeQuery();
			
			return readEntries(res, results);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DatabaseOperationException(
					"Error while executing sql query.", e);
		}
	}
	
	private static String getOperator(DBField field) {
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
			String query = "select * from " + form.getQualifiedName();
			query += " where ";
			
			// build qualification
			for(int i=0; i<e.getID().size(); i++) {
				query += e.getID().get(i).getDBField().getName();
				query += " = ?";
				if (i<e.getID().size()-1) {
					query += " and ";
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
			String query = "update " + form.getQualifiedName()
					+ " set ";
			// add values to the query
			for (int i=0; i<newValues.size(); i++) {
				query += newValues.get(i).getDBField().getName() + " = ?";
				if (i<newValues.size()-1)
					query += ", ";
				else
					query += " ";
			}
			
			// build query qualification
			query += " where ";
			for(int i=0; i<e.getID().size(); i++) {
				query += e.getID().get(i).getDBField().getName();
				query += " = ?";
				if (i<e.getID().size()-1) {
					query += " and ";
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
		String query = "insert into " + form.getQualifiedName() + " (";
		
		for (int i=0; i<e.getValues().size(); i++) {
			query += e.getValues().get(i).getDBField().getName();
			if (i<e.getValues().size()-1)
				query += ", ";
		}
		query += ") values (";
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
		String query = "delete from " + form.getQualifiedName() + " where ";
		
		// build qualification
		for(int i=0; i<e.getID().size(); i++) {
			query += e.getID().get(i).getDBField().getName();
			query += " = ?";
			if (i<e.getID().size()-1)
				query += " and ";
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

	/**
	 * Reads column value
	 * @param res - ResultSet contains data
	 * @param field - DBField
	 * @return Value<?> object
	 * @throws SQLException
	 */
	public static Value<?> getColumnValue(ResultSet res, DBField field)
			throws SQLException {
		switch (field.getDBType()) {
		case java.sql.Types.CHAR:
		case java.sql.Types.VARCHAR:
		case java.sql.Types.LONGVARCHAR:
			Value<String> val = new Value<String>(
					res.getString(field.getName()), field);
			return val;
		case java.sql.Types.NCHAR:
		case java.sql.Types.NVARCHAR:
		case java.sql.Types.LONGNVARCHAR:
			Value<String> nval = new Value<String>(
					res.getNString(field.getName()), field);
			return nval;
		case java.sql.Types.TINYINT:
		case java.sql.Types.SMALLINT:
		case java.sql.Types.INTEGER:
			Integer ival = res.getInt(field.getName());
			if (res.wasNull())
				return new Value<Integer>(null, field);
			return new Value<Integer>(ival, field);
		case java.sql.Types.DECIMAL:
		case java.sql.Types.NUMERIC:
			return new Value<BigDecimal>(res.getBigDecimal(field.getName()),
					field);
		case java.sql.Types.REAL:
		case java.sql.Types.FLOAT:
		case java.sql.Types.DOUBLE:
			Double dval = res.getDouble(field.getName());
			if (res.wasNull())
				return new Value<Double>(null, field);
			return new Value<Double>(dval, field);
		case java.sql.Types.BIGINT:
			Long lval = res.getLong(field.getName());
			if (res.wasNull())
				return new Value<Long>(null, field);
			return new Value<Long>(lval, field);
		case java.sql.Types.DATE:
			return new Value<Date>(res.getDate(field.getName()), field);
		case java.sql.Types.TIME:
			return new Value<Time>(res.getTime(field.getName()), field);
		case java.sql.Types.TIMESTAMP:
			return new Value<Timestamp>(res.getTimestamp(field.getName()), field);
		default:
			return new Value<Object>(res.getObject(field.getName()), field);
		}
	}
	
	private static void setParameters(PreparedStatement ps, Entry e) throws SQLException {
		for(int i=0; i<e.getID().size(); i++) {
			setParameter(ps, i+1, e.getID().get(i));
		}
	}
	
	private static void setParameters(PreparedStatement ps, List<Value<?>> values) 
			throws SQLException {
		for(int i=0; i<values.size(); i++) {
			setParameter(ps, i+1, values.get(i));
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void setParameter(PreparedStatement ps, int id, Value<?> v) throws SQLException {
		switch(v.getDBField().getDBType()) {
		case java.sql.Types.CHAR:
		case java.sql.Types.VARCHAR:
		case java.sql.Types.LONGVARCHAR:
			Value<String> str = (Value<String>) v;
			ps.setString(id, str.getValue());
			break;
		case java.sql.Types.NCHAR:
		case java.sql.Types.NVARCHAR:
		case java.sql.Types.LONGNVARCHAR:
			Value<String> nstr = (Value<String>) v;
			ps.setNString(id, nstr.getValue());
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
			ps.setBigDecimal(id, dec.getValue());
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