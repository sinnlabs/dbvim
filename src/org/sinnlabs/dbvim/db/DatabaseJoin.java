/**
 * 
 */
package org.sinnlabs.dbvim.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.sinnlabs.dbvim.db.exceptions.DatabaseOperationException;
import org.sinnlabs.dbvim.db.model.DBField;
import org.sinnlabs.dbvim.evaluator.DatabaseConditionBuilder;
import org.sinnlabs.dbvim.evaluator.exceptions.ParseException;
import org.sinnlabs.dbvim.form.FormFieldResolver;
import org.sinnlabs.dbvim.model.Form;
import org.sinnlabs.dbvim.model.ResultColumn;
import org.sinnlabs.dbvim.ui.IField;

/**
 * @author peter.liverovsky
 *
 */
public class DatabaseJoin extends Database {
	
	private Form form;
	
	private FormFieldResolver resolver;
	
	private DatabaseConditionBuilder conditionBuilder;
	
	/**
	 * List of primary id
	 */
	private List<DBField> leftId;
	private List<DBField> rightId;

	/**
	 * @param form
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws DatabaseOperationException
	 */
	public DatabaseJoin(Form form, FormFieldResolver resolver) throws DatabaseOperationException, ClassNotFoundException, SQLException {
		if (!form.isJoin())
			throw new IllegalArgumentException("Form should be join.");
		this.form = form;
		conditionBuilder = new DatabaseConditionBuilder();
		this.resolver = resolver;
		
		leftId = findId(resolver.getLeftResolver());
		rightId = findId(resolver.getRightResolver());
	}
	
	private List<DBField> findId(FormFieldResolver resolver) {		
		ArrayList<DBField> id = new ArrayList<DBField>();
		for(DBField f: resolver.getDBFields()) {
			if (f.isPrimaryKey())
				id.add(f);
		}

		return id;
	}
	
	@Override
	public List<Entry> queryAll(int limit) throws DatabaseOperationException {
		List<DBField> resultFields = new ArrayList<DBField>();
		// Add result list columns to select expression
		for(ResultColumn r : form.getResultList()) {
			resultFields.add(resolver.getFields().get(r.fieldName).getDBField());
		}
		// List of values for join condition
		List<Value<?>> values = new ArrayList<Value<?>>();
		// field aliases
		HashMap<DBField, String> aliases = new HashMap<DBField, String>();
		HashMap<DBField, String> leftAliases = new HashMap<DBField, String>();
		HashMap<DBField, String> rightAliases = new HashMap<DBField, String>();
		
		String query;
		try {
			query = buildJoinQuery(resultFields, aliases, values, leftAliases, rightAliases).query;
		} catch (ParseException e) {
			throw new DatabaseOperationException("Unable to build join query. " + e.getMessage(), e);
		}

		try {
			// connect to the db
			Connection db = DriverManager.getConnection(form.getDBConnection()
					.getConnectionString());
			
			// prepare statement
			PreparedStatement ps = db.prepareStatement(query);
			
			// set query parameters
			setParameters(ps, values);
			
			ResultSet res = ps.executeQuery();
			
			List<Entry> result = readEntries(res, resultFields, aliases, limit);
			res.close();
			
			return result;
		} catch (SQLException e1) {
			e1.printStackTrace();
			throw new DatabaseOperationException("Unable to query entry.", e1);
		}
	}
	
	@Override
	public List<Entry> query(List<Value<?>> condition, int limit) 
			throws DatabaseOperationException {
		List<DBField> resultFields = new ArrayList<DBField>();
		// Add result list columns to select expression
		for(ResultColumn r : form.getResultList()) {
			resultFields.add(resolver.getFields().get(r.fieldName).getDBField());
		}
		// List of values for join condition
		List<Value<?>> values = new ArrayList<Value<?>>();
		// field aliases
		HashMap<DBField, String> aliases = new HashMap<DBField, String>();
		HashMap<DBField, String> leftAliases = new HashMap<DBField, String>();
		HashMap<DBField, String> rightAliases = new HashMap<DBField, String>();
		
		JoinQuery query;
		try {
			query = buildJoinQuery(resultFields, aliases, values, leftAliases, rightAliases);
		} catch (ParseException e) {
			throw new DatabaseOperationException("Unable to build join query. " + e.getMessage(), e);
		}
		
		// build where condition
		query.query += " WHERE ";

		for(int i=0; i<condition.size(); i++) {
			if (isDBFieldsContains(resolver.getLeftResolver().getDBFields(), 
					condition.get(i).getDBField())) {
				
				query.query += query.leftFormAlias + ".";
				String alias = leftAliases.get(condition.get(i).getDBField());
				if (alias != null)
					query.query += alias;
				else
					query.query += condition.get(i).getDBField().getName();
				
				query.query += " " + getOperator(condition.get(i).getDBField()) + " ?";
			} else {
				query.query += query.rightFormAlias + ".";
				String alias = rightAliases.get(condition.get(i).getDBField());
				if (alias != null)
					query.query += alias;
				else
					query.query += condition.get(i).getDBField().getName(); 
				
				query.query += " " + getOperator(condition.get(i).getDBField()) + " ?";
			}
			values.add(condition.get(i));
			if ( i< condition.size()-1) {
				query.query += " AND ";
			}
		} // end build where
		
		try {
			// connect to the db
			Connection db = DriverManager.getConnection(form.getDBConnection()
					.getConnectionString());
			
			// prepare statement
			PreparedStatement ps = db.prepareStatement(query.query);
			
			// set query parameters
			setParameters(ps, values);
			
			ResultSet res = ps.executeQuery();
			
			List<Entry> result = readEntries(res, resultFields, aliases, limit);
			res.close();
			
			return result;
		} catch (SQLException e1) {
			e1.printStackTrace();
			throw new DatabaseOperationException("Unable to query entry.", e1);
		}
	}
	
	@Override
	public List<Entry> query(String query, List<IField<?>> allFields, int limit) 
			throws ParseException, DatabaseOperationException {
		List<DBField> resultFields = new ArrayList<DBField>();
		// Add result list columns to select expression
		for(ResultColumn r : form.getResultList()) {
			resultFields.add(resolver.getFields().get(r.fieldName).getDBField());
		}
		// List of values for join condition
		List<Value<?>> values = new ArrayList<Value<?>>();
		// field aliases
		HashMap<DBField, String> aliases = new HashMap<DBField, String>();
		HashMap<DBField, String> leftAliases = new HashMap<DBField, String>();
		HashMap<DBField, String> rightAliases = new HashMap<DBField, String>();
		
		JoinQuery joinQuery;
		try {
			joinQuery = buildJoinQuery(resultFields, aliases, values, leftAliases, rightAliases);
		} catch (ParseException e) {
			throw new DatabaseOperationException("Unable to build join query. " + e.getMessage(), e);
		}
		
		// build where condition
		joinQuery.query += " WHERE ";
		
		String dbCondition = conditionBuilder.buildCondition(query, null, resolver, values, 
				joinQuery.leftFormAlias, joinQuery.rightFormAlias, 
				leftAliases, rightAliases, false);
		
		joinQuery.query += dbCondition;
		
		try {
			// connect to the db
			Connection db = DriverManager.getConnection(form.getDBConnection()
					.getConnectionString());
			
			// prepare statement
			PreparedStatement ps = db.prepareStatement(joinQuery.query);
			
			// set query parameters
			setParameters(ps, values);
			
			ResultSet res = ps.executeQuery();
			
			List<Entry> result = readEntries(res, resultFields, aliases, limit);
			res.close();
			
			return result;
		} catch (SQLException e1) {
			e1.printStackTrace();
			throw new DatabaseOperationException("Unable to query entry.", e1);
		}
	}
	
	
	
	/**
	 * Build main join query for the form
	 * query example: 
	 * select [all primary id], [all payload fields] from form1 alias1 [inner, left] join form2 alias2 on condition
	 * 
	 * @param resultFields - Field to select (payload fields)
	 * @param aliases - [Out] map for field aliases
	 * @param values - [Out] values list for prepared statement
	 * @return String query
	 * @throws ParseException
	 */
	private JoinQuery buildJoinQuery(List<DBField> resultFields, HashMap<DBField, String> aliases, 
			List<Value<?>> values, HashMap<DBField, String> leftAliases, 
			HashMap<DBField, String> rightAliases) throws ParseException {
		// функция строит основной джоин запрос вида:
		// select [все primary id], [филды которые хочет видеть юзер] from left form join right form on [join condition]
		// alias generator for tables
		NameGenerator generator = new NameGenerator("t");
		// alias generator for fields
		NameGenerator fieldAliasGenerator = new NameGenerator("f");
		String query = "SELECT ";

		// Create select expression
		List<DBField> selectFields = new ArrayList<DBField>();
		
		// fill lists
		for (DBField f : leftId) {
			selectFields.add(f);
			aliases.put(f, fieldAliasGenerator.getNext());
		}
		for (DBField f : rightId) {
			selectFields.add(f);
			aliases.put(f, fieldAliasGenerator.getNext());
		}

		// Add result columns to select expression
		for(DBField f : resultFields) {
			selectFields.add(f);
			aliases.put(f, fieldAliasGenerator.getNext());
		}
		
		SubQuery leftSubQuery;
		leftSubQuery = buildSubQuery(selectFields, generator, fieldAliasGenerator, 
				resolver.getLeftResolver(), leftAliases);
		
		SubQuery rightSubQuery;
		rightSubQuery = buildSubQuery(selectFields, generator, fieldAliasGenerator, 
				resolver.getRightResolver(), rightAliases);
		
		// create select statement
		for (int i=0; i<selectFields.size(); i++) {
			DBField f = selectFields.get(i);
			if (isDBFieldsContains(resolver.getLeftResolver().getDBFields(), f)) {
				String alias = leftAliases.get(f);
				if (alias == null)
					query += leftSubQuery.alias + "." + f.getName() + " " + aliases.get(f);
				else
					query += leftSubQuery.alias + "." + alias + " " + aliases.get(f);
			} else if (isDBFieldsContains(resolver.getRightResolver().getDBFields(), f)) {
				String alias = rightAliases.get(f);
				if (alias == null)
					query += rightSubQuery.alias + "." + f.getName() + " " + aliases.get(f);
				else
					query += rightSubQuery.alias + "." + alias + " " + aliases.get(f);
			}
			if (i<selectFields.size()-1)
				query += ", ";
			else
				query += " ";
		}
		query += " FROM ";
		// add join sub queries
		List<Value<?>> conditionValues = new ArrayList<Value<?>>();
		query += leftSubQuery.query + getJoinString() + rightSubQuery.query + " ON ";
		// build join on condition
		query += conditionBuilder.buildCondition(form.getJoinClause(), null, 
				resolver, conditionValues, leftSubQuery.alias, 
				rightSubQuery.alias, leftAliases, rightAliases, true);

		// build all values for the query
		if (leftSubQuery.sorted != null)
			values.addAll(leftSubQuery.sorted);
		if (rightSubQuery.sorted != null)
			values.addAll(rightSubQuery.sorted);
		values.addAll(conditionValues);
		
		JoinQuery res = new JoinQuery();
		res.query = query;
		res.leftFormAlias = leftSubQuery.alias;
		res.rightFormAlias = rightSubQuery.alias;
		return res;
	}
	
	private SubQuery buildSubQuery(List<DBField> fields, NameGenerator generator, NameGenerator aliasGenerator,
			FormFieldResolver resolver, HashMap<DBField, String> aliases) throws ParseException {
		if (resolver.getForm().isJoin()) {
			String query = "SELECT ";
			// prepare left and right form fields list
			List<DBField> leftFields = new ArrayList<DBField>();
			List<DBField> rightFields = new ArrayList<DBField>();
			// determine which form field belongs (left or right)
			for(DBField f : fields) {
				if ( isDBFieldsContains(resolver.getLeftResolver().getDBFields(), f) ) {
					leftFields.add(f);
				}
				if ( isDBFieldsContains(resolver.getRightResolver().getDBFields(), f) ) {
					rightFields.add(f);
				}
			}
			HashMap<DBField, String> leftAliases = new HashMap<DBField, String>();
			HashMap<DBField, String> rightAliases = new HashMap<DBField, String>();
			// get sub queries for each form
			SubQuery leftSubQuery = buildSubQuery(fields, generator, aliasGenerator, 
					resolver.getLeftResolver(), leftAliases);
			SubQuery rightSubQuery = buildSubQuery(fields, generator, aliasGenerator, 
					resolver.getRightResolver(), rightAliases);
			// create condition values list
			List<Value<?>> conditionValues = new ArrayList<Value<?>>();
			String tmp = " FROM " + leftSubQuery.query + getJoinString() + rightSubQuery.query + " ON ";
			// build condition
			tmp += conditionBuilder.buildCondition(resolver.getForm().getJoinClause(), null, 
					resolver, conditionValues, leftSubQuery.alias,
					rightSubQuery.alias, leftAliases, rightAliases, true);
			for(DBField f : leftFields) {
				aliases.put(f, aliasGenerator.getNext());
				String alias = leftAliases.get(f);
				if (alias == null) {
					query += leftSubQuery.alias + "." + f.getName() + " " + 
							aliases.get(f) + ", ";
				} else {
					query += leftSubQuery.alias + "." + alias + " " + 
							aliases.get(f) + ", ";
				}
			}
			for(int i=0; i<rightFields.size(); i++) {
				aliases.put(rightFields.get(i), aliasGenerator.getNext());
				String alias = rightAliases.get(rightFields.get(i));
				if (alias == null) {
					query += rightSubQuery.alias + "." + rightFields.get(i).getName() + 
							" " + aliases.get(rightFields.get(i));
				} else {
					query += rightSubQuery.alias + "." + alias + 
							" " + aliases.get(rightFields.get(i));
				}
				if (i<rightFields.size()-1)
					query += ", ";
				else
					query += " ";
			}
			query += tmp;
			SubQuery res = new SubQuery();
			res.alias = generator.getNext();
			res.query = "(" + query + ") " + res.alias;
			res.sorted = new ArrayList<Value<?>>();
			if (leftSubQuery.sorted != null)
				res.sorted.addAll(leftSubQuery.sorted);
			if (rightSubQuery.sorted != null)
				res.sorted.addAll(rightSubQuery.sorted);
			res.sorted.addAll(conditionValues);
			return res;
		} else {
			SubQuery q = new SubQuery();
			q.alias = generator.getNext();
			q.query = resolver.getForm().getQualifiedName() + " " + q.alias;
			return q;
		}
	}
	
	private String getJoinString() {
		if (form.isOuterJoin())
			return " LEFT OUTER JOIN ";
		return " INNER JOIN ";
	}
	
	private boolean isDBFieldsContains(List<DBField> fields, DBField f) {
		for (DBField t : fields) {
			if (t.getFullName().equals(f.getFullName())) {
				return true;
			}
		}
		return false;
	}
	
	private class SubQuery {
		String query;
		String alias;
		List<Value<?>> sorted;
	}
	
	private class JoinQuery {
		String query;
		String leftFormAlias;
		String rightFormAlias;
	}
	
	public Entry readEntry(Entry e) throws DatabaseOperationException {
		List<DBField> resultFields = new ArrayList<DBField>();
		// Add result list columns to select expression
		for(IField<?> f : resolver.getFields().values()) {
			resultFields.add(f.getDBField());
		}
		// List of values for join condition
		List<Value<?>> values = new ArrayList<Value<?>>();
		// field aliases
		HashMap<DBField, String> aliases = new HashMap<DBField, String>();
		// aliases for sub queries
		HashMap<DBField, String> leftAliases = new HashMap<DBField, String>();
		HashMap<DBField, String> rightAliases = new HashMap<DBField, String>();
		
		JoinQuery joinQuery;
		try {
			joinQuery = buildJoinQuery(resultFields, aliases, values, leftAliases, rightAliases);
		} catch (ParseException e1) {
			throw new DatabaseOperationException("Unable to build join query. " + e1.getMessage(), e1);
		}
		
		// build where condition
		String query = joinQuery.query + " WHERE ";
		
		for(int i=0; i<e.getID().size(); i++) {
			if (isDBFieldsContains(leftId, e.getID().get(i).getDBField())) {
				query += joinQuery.leftFormAlias + ".";
				String alias = leftAliases.get(e.getID().get(i).getDBField());
				if (alias != null)
					query += alias;
				else
					query += e.getID().get(i).getDBField().getName();
				if (e.getID().get(i).getValue() != null) {
					  query += " = ?";
					values.add(e.getID().get(i));
				} else {
					query += " IS NULL";
				}
			} else {
				query += joinQuery.rightFormAlias + ".";
				String alias = rightAliases.get(e.getID().get(i).getDBField());
				if (alias != null)
					query += alias;
				else
					query += e.getID().get(i).getDBField().getName(); 
				if (e.getID().get(i).getValue() != null) {
					query += " = ?";
					values.add(e.getID().get(i));
				} else {
					query += " IS NULL";
				}
			}
			
			if ( i< e.getID().size()-1) {
				query += " AND ";
			}
		}
		
		try {
			// connect to the db
			Connection db = DriverManager.getConnection(form.getDBConnection()
					.getConnectionString());
			
			// prepare statement
			PreparedStatement ps = db.prepareStatement(query);

			// set query parameters
			setParameters(ps, values);

			ResultSet set = ps.executeQuery();

			List<Entry> entries = readEntries(set, resultFields, aliases, 1);
			
			set.close();
			
			if (entries.size() > 0)
				return entries.get(0);
			
			// if no entries found
			return null;
		} catch (SQLException e1) {
			e1.printStackTrace();
			throw new DatabaseOperationException("Error while executing sql query.", e1);
		}
	}
	
	/**
	 * Reads entries from ResultSet
	 * @param results - ResultSet contains row data
	 * @param selectFields - DBField list of the select expression (does not contains primary key fields) 
	 * @param aliases - DBField aliases of the select expression
	 * @param limit - Maximum number of rows to read or 0 if no max specified
	 * @return List of Entries
	 * @throws SQLException
	 */
	private List<Entry> readEntries(ResultSet results, List<DBField> selectFields, 
			HashMap<DBField, String> aliases, int limit) throws SQLException {
		
		List<Entry> entries = new ArrayList<Entry>();
		int count = 0;
		while (results.next()) {
			count++;
			Entry entry = new Entry();
			// read primary key
			for (DBField f : leftId) {
				entry.getID().add(
						getColumnValue(results, f, aliases.get(f)));
			}
			for (DBField f : rightId) {
				entry.getID().add(
						getColumnValue(results, f, aliases.get(f)));
			}
			// read data
			for (DBField f : selectFields) {
				entry.getValues().add(
						getColumnValue(results, f, aliases.get(f)));
			}
			entries.add(entry);
			
			if (limit != 0 && count > limit)
				break;
		}
		return entries;
	}

}
