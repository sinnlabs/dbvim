/**
 * 
 */
package org.sinnlabs.dbvim.script;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.sinnlabs.dbvim.config.ConfigLoader;
import org.sinnlabs.dbvim.db.Database;
import org.sinnlabs.dbvim.db.DatabaseFactory;
import org.sinnlabs.dbvim.db.Entry;
import org.sinnlabs.dbvim.db.Value;
import org.sinnlabs.dbvim.evaluator.AbstractVariableSet;
import org.sinnlabs.dbvim.evaluator.DatabaseConditionBuilder;
import org.sinnlabs.dbvim.form.FormFieldResolver;
import org.sinnlabs.dbvim.form.FormFieldResolverFactory;
import org.sinnlabs.dbvim.model.Form;
import org.sinnlabs.dbvim.ui.IField;
import org.sinnlabs.dbvim.zk.SearchComposer;

/**
 * Class represents form script api interface
 * @author peter.liverovsky
 *
 */
public class ScriptApi {

	SearchComposer composer;
	
	/**
	 * Creates ScriptApi instance
	 * @param composer Current search composer
	 */
	public ScriptApi(SearchComposer composer) {
		this.composer = composer;
	}
	
	/**
	 * Creates a new entry in the form
	 * @param formName Form name
	 * @param entry Entry object contains column values
	 * @throws Exception 
	 */
	public void pushEntry(String formName, Entry entry) throws Exception {
		Form f = getForm(formName);
		if (f!= null) {
			FormFieldResolver resolver = FormFieldResolverFactory.getResolver(f);
			Database db = DatabaseFactory.createInstance(f, resolver);
			db.insertEntry(entry);
		} else {
			throw new IllegalArgumentException("Form does not exists.");
		}
	}
	
	/**
	 * Creates a new entry in the form
	 * @param formName form name
	 * @param r Record contains form values
	 * @throws Exception 
	 */
	public void pushEntry(String formName, Record r) throws Exception {
		Form f = getForm(formName);
		if (f!= null) {
			FormFieldResolver resolver = FormFieldResolverFactory.getResolver(f);
			Database db = DatabaseFactory.createInstance(f, resolver);
			Entry e = new Entry();
			// fill the Entry object
			for ( java.util.Map.Entry<String, Object> i : r.getValues().entrySet()) {
				IField<?> field = resolver.getFields().get(i.getKey());
				
				if (field == null)
					throw new IllegalArgumentException("Unknown field: " + i.getKey() + 
							" on the form: " + f);
				Value<?> dbVal = null;
				if (i.getValue() == null || i.getValue() instanceof String) {
					dbVal = field.fromString((String)i.getValue());
				} else if (i.getValue() instanceof Value<?>) {
					dbVal = (Value<?>) i.getValue();
				} else {
					dbVal = field.fromObject(i.getValue());
				}
				e.getValues().add(dbVal);
				if (field.getDBField().isPrimaryKey())
					e.getID().add(dbVal);
			}
			db.insertEntry(e);
		} else {
			throw new IllegalArgumentException("Form does not exists.");
		}
	}
	
	/**
	 * Query entries from the database
	 * @param formName Form name
	 * @param qualification search qualification string
	 * @return List of entries
	 * @throws Exception 
	 */
	public List<Record> query(String formName, String qualification) throws Exception {
		AbstractVariableSet<Value<?>> vars = 
				DatabaseConditionBuilder.buildVariablesFromFields(composer.getFields());
		Form f = getForm(formName);
		if (f!= null) {
			FormFieldResolver resolver = FormFieldResolverFactory.getResolver(f);
			Database db = DatabaseFactory.createInstance(f, resolver);
			
			List<IField<?>> allFields = new ArrayList<IField<?>>();
			allFields.addAll(resolver.getFields().values());
			
			List<Entry> entries = db.query(allFields, qualification, 0, vars);
			List<Record> records = new ArrayList<Record>(entries.size());
			for (Entry e : entries) {
				Record rec = new Record();
				for (Value<?> v : e.getValues()) {
					rec.getValues().put(resolver.findByDBField(v.getDBField()).getId(), v.getValue());
				}
				records.add(rec);
			}
			return records;
		} else {
			throw new IllegalArgumentException("Form does not exists.");
		}
	}
	
	/**
	 * Updates the existing entry
	 * @param formName Form name
	 * @param entry Entry contains valid id
	 * @throws Exception 
	 */
	public void updateEntry(String formName, Entry entry) throws Exception {
		Form f = getForm(formName);
		if (f!= null) {
			FormFieldResolver resolver = FormFieldResolverFactory.getResolver(f);
			Database db = DatabaseFactory.createInstance(f, resolver);
			
			db.updateEntry(entry, entry.getValues());
		} else {
			throw new IllegalArgumentException("Form does not exists.");
		}
	}
	
	/**
	 * Updates all matching request with values
	 * @param formName Form name
	 * @param qualification Update query qualification
	 * @param values new values
	 * @throws Exception
	 */
	public void updateEntry(String formName, String qualification, Record values) throws Exception {
		AbstractVariableSet<Value<?>> vars = 
				DatabaseConditionBuilder.buildVariablesFromFields(composer.getFields());
		Form f = getForm(formName);
		if (f!= null) {
			FormFieldResolver resolver = FormFieldResolverFactory.getResolver(f);
			Database db = DatabaseFactory.createInstance(f, resolver);
			// Fill db values list
			List<Value<?>> newValues = new ArrayList<Value<?>>(values.getValues().size());
			for (java.util.Map.Entry<String, Object> entry : values.getValues().entrySet()) {
				Value<?> v = resolver.getFields().get(entry.getKey()).fromObject(entry.getValue());
				newValues.add(v);
			}
			
			db.update(newValues, qualification, vars);
		} else {
			throw new IllegalArgumentException("Form does not exists.");
		}
	}
	
	/**
	 * Deletes entry
	 * @param formName Form name
	 * @param entry Entry contains valid id
	 * @throws Exception 
	 */
	public void deleteEntry(String formName, Entry entry) throws Exception {
		Form f = getForm(formName);
		if (f!=null) {
			FormFieldResolver resolver = FormFieldResolverFactory.getResolver(f);
			Database db = DatabaseFactory.createInstance(f, resolver);
			
			db.deleteEntry(entry);
		} else {
			throw new IllegalArgumentException("Form does not exists.");
		}
	}
	
	/**
	 * Deletes entry
	 * @param formName Form name
	 * @param r Record to be deleted
	 * @throws Exception
	 */
	public void deleteEntry(String formName, Record r) throws Exception {
		Form f = getForm(formName);
		if (f!= null) {
			FormFieldResolver resolver = FormFieldResolverFactory.getResolver(f);
			Database db = DatabaseFactory.createInstance(f, resolver);
			
			Entry e = new Entry();
			// fill the Entry object
			for ( java.util.Map.Entry<String, Object> i : r.getValues().entrySet()) {
				IField<?> field = resolver.getFields().get(i.getKey());
				
				if (field == null)
					throw new IllegalArgumentException("Unknown field: " + i.getKey() + 
							" on the form: " + f);
				Value<?> dbVal = null;
				if (i.getValue() == null || i.getValue() instanceof String) {
					dbVal = field.fromString((String)i.getValue());
				} else if (i.getValue() instanceof Value<?>) {
					dbVal = (Value<?>) i.getValue();
				} else {
					dbVal = field.fromObject(i.getValue());
				}
				e.getValues().add(dbVal);
				if (field.getDBField().isPrimaryKey())
					e.getID().add(dbVal);
			}
			
			db.deleteEntry(e);
		} else {
			throw new IllegalArgumentException("Form does not exists.");
		}
	}
	
	/**
	 * Deletes all matching entries from the form
	 * @param formName Form name
	 * @param qualification search qualification string
	 */
	public void deleteEntries(String formName, String qualification) {
		
	}
	
	/**
	 * Executes sql command directly.
	 * You can use parameterized query in JDBC style (select * from table where id = ?)
	 * @param sqlCommand The sql command
	 * @param params Parameters for parameterized query
	 */
	public void directSQL(String sqlCommand, Value<?> ...params) {
		
	}
	
	private Form getForm(String fName) throws SQLException {
		return ConfigLoader.getInstance().getForms().queryForId(fName);
	}
}
