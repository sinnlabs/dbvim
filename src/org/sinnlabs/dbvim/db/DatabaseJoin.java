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
import org.sinnlabs.dbvim.evaluator.AbstractVariableSet;
import org.sinnlabs.dbvim.evaluator.DatabaseConditionBuilder;
import org.sinnlabs.dbvim.evaluator.exceptions.ParseException;
import org.sinnlabs.dbvim.form.FormFieldResolver;
import org.sinnlabs.dbvim.model.Form;
import org.sinnlabs.dbvim.model.ResultColumn;
import org.sinnlabs.dbvim.ui.IField;

/**
 * Class that manages database operations for the join form.
 * @author peter.liverovsky
 *
 */
public class DatabaseJoin extends Database {
	
	private Form form;
	
	private FormFieldResolver resolver;
	
	private DatabaseConditionBuilder conditionBuilder;
	
	/**
	 * List of primary id's for each form
	 */
	private List<DBField> leftId;
	private List<DBField> rightId;

	/**
	 * @param form
	 * @throws ClassNotFoundException
	 * @throws SQLException
	 * @throws DatabaseOperationException
	 */
	/*package*/ DatabaseJoin(Form form, FormFieldResolver resolver) throws DatabaseOperationException, ClassNotFoundException, SQLException {
		if (!form.isJoin())
			throw new IllegalArgumentException("Form should be join.");
		
		this.form = form;
		conditionBuilder = new DatabaseConditionBuilder();
		this.resolver = resolver;
		
		// Get rimary id's for each form
		leftId = findId(resolver.getLeftResolver());
		rightId = findId(resolver.getRightResolver());
	}
	
	/**
	 * Finds primary id fields
	 * @param resolver - FormFieldResolver for the form
	 * @return List of DBField that are primary id
	 */
	private List<DBField> findId(FormFieldResolver resolver) {		
		ArrayList<DBField> id = new ArrayList<DBField>();
		
		// walk through all DBFields of the form
		for(DBField f: resolver.getDBFields()) {
			// Checks if the field is Primary id
			if (f.isPrimaryKey())
				id.add(f);
		}

		return id;
	}
	
	@Override
	public List<Entry> queryAll(List<IField<?>> fields, int limit) throws DatabaseOperationException {
		// List of result fields.
		// Add result list columns to select expression
		List<DBField> resultFields = getPayloadFields(fields);
		
		// List of sorted values for join condition
		// This values filled by the DatabaseConditionBuilder
		List<Value<?>> values = new ArrayList<Value<?>>();
		
		// result field aliases
		HashMap<DBField, String> aliases = new HashMap<DBField, String>();
		// left join sub query field aliases (select field alias, ...)
		HashMap<DBField, String> leftAliases = new HashMap<DBField, String>();
		// right join sub query field aliases
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
			
			// execute query
			ResultSet res = ps.executeQuery();
			
			List<Entry> result = readEntries(res, resultFields, aliases, limit);
			res.close();
			
			ps.close();
			db.close();
			
			return result;
		} catch (SQLException e1) {
			e1.printStackTrace();
			throw new DatabaseOperationException("Unable to query entry.", e1);
		}
	}
	
	@Override
	public List<Entry> query(List<IField<?>> fields, List<Value<?>> condition, int limit) 
			throws DatabaseOperationException {
		// Add result list columns to select expression
		List<DBField> resultFields = getPayloadFields(fields);
		
		
		// List of sorted values for join condition
		// This values filled by the DatabaseConditionBuilder
		List<Value<?>> values = new ArrayList<Value<?>>();

		// result field aliases
		HashMap<DBField, String> aliases = new HashMap<DBField, String>();
		// left join sub query field aliases (select field alias, ...)
		HashMap<DBField, String> leftAliases = new HashMap<DBField, String>();
		// right join sub query field aliases
		HashMap<DBField, String> rightAliases = new HashMap<DBField, String>();

		JoinQuery query;
		try {
			query = buildJoinQuery(resultFields, aliases, values, leftAliases, rightAliases);
		} catch (ParseException e) {
			throw new DatabaseOperationException("Unable to build join query. " + e.getMessage(), e);
		}
		
		// build where condition
		query.query += " WHERE ";

		// Walk through all condition values
		for(int i=0; i<condition.size(); i++) {
			// if condition value sets the left field
			if (isDBFieldsContains(resolver.getLeftResolver().getDBFields(), 
					condition.get(i).getDBField())) {
				// build where qualification like: formAlias.{DBField Name|FieldAlias}
				query.query += query.leftFormAlias + ".";
				
				// check if the field alias exists
				String alias = leftAliases.get(condition.get(i).getDBField());
				if (alias != null)
					query.query += alias;
				else
					query.query += condition.get(i).getDBField().getName();
				
				query.query += " " + getOperator(condition.get(i).getDBField()) + " ?";
			
			// if condition value sets the right field
			} else {
				// same as for the left form
				query.query += query.rightFormAlias + ".";
				String alias = rightAliases.get(condition.get(i).getDBField());
				if (alias != null)
					query.query += alias;
				else
					query.query += condition.get(i).getDBField().getName(); 
				
				query.query += " " + getOperator(condition.get(i).getDBField()) + " ?";
			}
			// add value to the end of the condition values list
			values.add(condition.get(i));
			
			// add AND to the query if the condition value is not last 
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
			
			ps.close();
			db.close();
			
			return result;
		} catch (SQLException e1) {
			e1.printStackTrace();
			throw new DatabaseOperationException("Unable to query entry.", e1);
		}
	}
	
	@Override
	public List<Entry> query(List<IField<?>> fields, String query, 
			int limit, AbstractVariableSet<Value<?>> context) throws ParseException, DatabaseOperationException {
		// Add result list columns to select expression
		List<DBField> resultFields = getPayloadFields(fields);

		
		// List of sorted values for join condition
		// This values filled by the DatabaseConditionBuilder
		List<Value<?>> values = new ArrayList<Value<?>>();

		// result field aliases
		HashMap<DBField, String> aliases = new HashMap<DBField, String>();
		// left join sub query field aliases (select field alias, ...)
		HashMap<DBField, String> leftAliases = new HashMap<DBField, String>();
		// right join sub query field aliases
		HashMap<DBField, String> rightAliases = new HashMap<DBField, String>();

		JoinQuery joinQuery;
		try {
			joinQuery = buildJoinQuery(resultFields, aliases, values, leftAliases, rightAliases);
		} catch (ParseException e) {
			throw new DatabaseOperationException("Unable to build join query. " + e.getMessage(), e);
		}
		
		// build where condition
		joinQuery.query += " WHERE ";
		
		String dbCondition = conditionBuilder.buildCondition(query, context, resolver, values, 
				joinQuery.leftFormAlias, joinQuery.rightFormAlias, 
				leftAliases, rightAliases, false);
		
		// Add where qualification to the end of the join query
		joinQuery.query += dbCondition;
		
		try {
			// connect to the db
			Connection db = DriverManager.getConnection(form.getDBConnection()
					.getConnectionString());
			
			// prepare statement
			PreparedStatement ps = db.prepareStatement(joinQuery.query);
			
			// set query parameters
			setParameters(ps, values);
			
			// execute query
			ResultSet res = ps.executeQuery();
			
			List<Entry> result = readEntries(res, resultFields, aliases, limit);
			res.close();
			
			ps.close();
			db.close();
			
			return result;
		} catch (SQLException e1) {
			e1.printStackTrace();
			throw new DatabaseOperationException("Unable to query entry.", e1);
		}
	}
	
	/**
	 * Build pay load DBField list
	 * @param fields payload fields
	 * @return List of DBField
	 */
	private List<DBField> getPayloadFields(List<IField<?>> fields) {
		List<DBField> res = new ArrayList<DBField>();
		// if payload fields not sepcified
		if (fields == null) {
			// Add result list columns to select expression
			for(ResultColumn r : form.getResultList()) {
				res.add(resolver.getFields().get(r.fieldName).getDBField());
			}
		} else {
			for (IField<?> f : fields) {
				res.add(f.getDBField());
			}
		}
		return res;
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
		
		// The query string
		String query = "SELECT ";

		/*** Create the select expression ***/
		List<DBField> selectFields = new ArrayList<DBField>();
		
		// First add all primary id's to the select expression
		// And save field alias in the field alias map
		for (DBField f : leftId) {
			selectFields.add(f);
			aliases.put(f, fieldAliasGenerator.getNext());
		}
		for (DBField f : rightId) {
			selectFields.add(f);
			aliases.put(f, fieldAliasGenerator.getNext());
		}

		// Then add all payload fields (result fields) to the select expression
		for(DBField f : resultFields) {
			selectFields.add(f);
			aliases.put(f, fieldAliasGenerator.getNext());
		}
		
		// Remeber payload fields size
		int selectFieldsSize = selectFields.size();
		// Then add all fields from the condition.
		// for example select t1.ID from f1 t1 inner join (select ID f2 from f2 t2) on t2.Name = t1.Name
		// In this case we need to add the condition field t2.Name to the sub query select statement
		for (IField<?> f : conditionBuilder.getConditionFields(form.getJoinClause(), 
				resolver, true)) {
			selectFields.add(f.getDBField());
		}
		
		/*** build join sub queries for the left and right forms ***/
		SubQuery leftSubQuery;
		leftSubQuery = buildSubQuery(selectFields, generator, fieldAliasGenerator, 
				resolver.getLeftResolver(), leftAliases);
		
		SubQuery rightSubQuery;
		rightSubQuery = buildSubQuery(selectFields, generator, fieldAliasGenerator, 
				resolver.getRightResolver(), rightAliases);
		
		
		/*** Create the select statement ***/
		// Walk through select expression fields and add each field to the select statement
		for (int i=0; i<selectFieldsSize; i++) {
			// Get the DBField
			DBField f = selectFields.get(i);
			
			// If the DBField belongs to the left form
			if (isDBFieldsContains(resolver.getLeftResolver().getDBFields(), f)) {
				// Build field statement like: leftFormAlias.{DBFieldName|FieldAlias}
				// Get the field alias
				String alias = leftAliases.get(f);
				if (alias == null){ // If the field alias does not exists (this means that left form is a basic form)
					// Build select statement using DBField name
					query += leftSubQuery.alias + "." + f.getName() + " " + aliases.get(f);
				} else {
					// Build select statement using DBField alias name
					query += leftSubQuery.alias + "." + alias + " " + aliases.get(f);
				}
			// If the field belongs to the right form
			} else if (isDBFieldsContains(resolver.getRightResolver().getDBFields(), f)) {
				// Build select statement same as for the left form
				String alias = rightAliases.get(f);
				if (alias == null)
					query += rightSubQuery.alias + "." + f.getName() + " " + aliases.get(f);
				else
					query += rightSubQuery.alias + "." + alias + " " + aliases.get(f);
			}
			
			// add ', ' to the end of the select statement if the DBField is not last
			if (i<selectFieldsSize-1)
				query += ", ";
			else // otherwise add ' '
				query += " ";
		}
		
		/*** build FROM statement ***/
		query += " FROM ";
		
		// add join sub queries to the statement
		query += leftSubQuery.query + getJoinString(resolver) + rightSubQuery.query + " ON ";
		
		// build join on qualification
		List<Value<?>> conditionValues = new ArrayList<Value<?>>();
		query += conditionBuilder.buildCondition(form.getJoinClause(), null, 
				resolver, conditionValues, leftSubQuery.alias, 
				rightSubQuery.alias, leftAliases, rightAliases, true);

		
		// Add all conditions values to values list
		// Order should be from left to the right
		// select .. from leftSubQuery join rightSubQuery on qualification
		if (leftSubQuery.sorted != null)
			values.addAll(leftSubQuery.sorted);
		if (rightSubQuery.sorted != null)
			values.addAll(rightSubQuery.sorted);
		values.addAll(conditionValues);
		
		// return the builded join query
		JoinQuery res = new JoinQuery();
		res.query = query;
		res.leftFormAlias = leftSubQuery.alias;
		res.rightFormAlias = rightSubQuery.alias;
		return res;
	}
	
	private SubQuery buildSubQuery(List<DBField> fields, NameGenerator generator, NameGenerator aliasGenerator,
			FormFieldResolver resolver, HashMap<DBField, String> aliases) throws ParseException {
		// if the form is a join form
		if (resolver.getForm().isJoin()) {
			
			// select statement
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
			
			// prepare left and right forms field aliases
			HashMap<DBField, String> leftAliases = new HashMap<DBField, String>();
			HashMap<DBField, String> rightAliases = new HashMap<DBField, String>();
			
			// get sub queries for each form
			SubQuery leftSubQuery = buildSubQuery(fields, generator, aliasGenerator, 
					resolver.getLeftResolver(), leftAliases);
			SubQuery rightSubQuery = buildSubQuery(fields, generator, aliasGenerator, 
					resolver.getRightResolver(), rightAliases);
			
			// create condition values list
			List<Value<?>> conditionValues = new ArrayList<Value<?>>();
			String tmp = " FROM " + leftSubQuery.query + getJoinString(resolver) + rightSubQuery.query + " ON ";
			
			// build condition
			tmp += conditionBuilder.buildCondition(resolver.getForm().getJoinClause(), null, 
					resolver, conditionValues, leftSubQuery.alias,
					rightSubQuery.alias, leftAliases, rightAliases, true);
			for(DBField f : leftFields) {
				aliases.put(f, aliasGenerator.getNext());
				String alias = leftAliases.get(f);
				if (alias == null) {
					query += leftSubQuery.alias + ".\"" + f.getName() + "\" " + 
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
			
			// add condition values
			// order should be left to the right
			if (leftSubQuery.sorted != null)
				res.sorted.addAll(leftSubQuery.sorted);
			if (rightSubQuery.sorted != null)
				res.sorted.addAll(rightSubQuery.sorted);
			res.sorted.addAll(conditionValues);
			
			// return builded sub query
			return res;
		} else {
			// if the form is not a join form
			// then we do not add full query (with select statement)
			// just add the form alias
			SubQuery q = new SubQuery();
			q.alias = generator.getNext();
			q.query = resolver.getForm().getQualifiedName() + " " + q.alias;
			return q;
		}
	}
	
	private String getJoinString(FormFieldResolver resolver) {
		if (resolver.getForm().isOuterJoin())
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
	
	@Override
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
					query += "\"" + e.getID().get(i).getDBField().getName() + "\"";
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
					query += "\"" + e.getID().get(i).getDBField().getName() + "\""; 
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
			ps.close();
			db.close();
			
			if (entries.size() > 0)
				return entries.get(0);
			
			// if no entries found
			return null;
		} catch (SQLException e1) {
			e1.printStackTrace();
			throw new DatabaseOperationException("Error while executing sql query.", e1);
		}
	}
	
	@Override
	public void updateEntry(Entry e, List<Value<?>> values) throws DatabaseOperationException {
		updateRecord(e, values, resolver);
	}
	
	@Override
	public void update(List<Value<?>> values, String query, 
			AbstractVariableSet<Value<?>> context) throws ParseException, DatabaseOperationException {
		throw new DatabaseOperationException("Operation not supported.", null);
	}
	
	@Override
	public void update(List<Value<?>> condition, List<Value<?>> values) 
			throws DatabaseOperationException {
		throw new DatabaseOperationException("Operation not supported.", null);
	}
	
	@Override
	public void updateAll(List<Value<?>> values) throws DatabaseOperationException {
		throw new DatabaseOperationException("Operation not supported.", null);
	}
	
	private void updateRecord(Entry e, List<Value<?>> values, FormFieldResolver r) throws DatabaseOperationException {
		if (r.getForm().isJoin()) {
			updateRecord(e, values, r.getLeftResolver());
			updateRecord(e, values, r.getRightResolver());
		} else {
			// Get the form values
			List<Value<?>> formValues = new ArrayList<Value<?>>();
			for(Value<?> v : values) {
				if (isDBFieldsContains(r.getDBFields(), v.getDBField())) {
					formValues.add(v);
				}
			}
			// If no form values found
			if (formValues.isEmpty())
				return;
			
			// find updated values
			List<Value<?>> newValues = new ArrayList<Value<?>>();
			for(Value<?> nv : formValues) {
				for(Value<?> ov : e.getValues()) {
					// if new value is different
					if (nv.getDBField().getFullName().equals(ov.getDBField().getFullName())
							&& (ov.getValue() == null || !nv.getValue().equals(ov.getValue())) ) {
						// add new value to the list
						newValues.add(nv);
					}
				}
			}
			
			// if no value updated, return
			if (newValues.size() == 0)
				return;
			
			// Find the form primary id
			List<Value<?>> id = new ArrayList<Value<?>>();
			for(Value<?> v : e.getID()) {
				if (isDBFieldsContains(r.getDBFields(), v.getDBField())) {
					id.add(v);
				}
			}
			
			// Connect to the db
			try {
				Connection db = DriverManager.getConnection(r.getForm().getDBConnection()
						.getConnectionString());
			
				// build update query
				String query = "UPDATE " + r.getForm().getQualifiedName()
						+ " SET ";
				// add values to the query
				for (int i=0; i<newValues.size(); i++) {
					query += "\"" + newValues.get(i).getDBField().getName() + "\" = ?";
					if (i<newValues.size()-1)
						query += ", ";
					else
						query += " ";
				}
				
				// build query qualification
				query += " WHERE ";
				for(int i=0; i<id.size(); i++) {
					query += "\"" + id.get(i).getDBField().getName() + "\"";
					query += " = ?";
					if (i<id.size()-1) {
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
				
				//release resources
				ps.close();
				db.close();
			} catch (SQLException e1) {
				System.err.println("ERROR: Unable to update entry: ");
				e1.printStackTrace();
				throw new DatabaseOperationException("Unable to update entry.", e1);
			}
		}
	}
	
	@Override
	public void insertEntry(Entry e) throws DatabaseOperationException {
		// it is not possible to add a new entry into a join form
		throw new DatabaseOperationException("Operation not supported.", null);
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
