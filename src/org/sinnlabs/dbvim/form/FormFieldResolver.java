/**
 * 
 */
package org.sinnlabs.dbvim.form;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.sinnlabs.dbvim.db.model.DBField;
import org.sinnlabs.dbvim.db.model.DBModel;
import org.sinnlabs.dbvim.model.Form;
import org.sinnlabs.dbvim.ui.IField;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zul.Idspace;

/**
 * Used to resolve field mapping to DBField
 * @author peter.liverovsky
 *
 */
public class FormFieldResolver {
	
	private Form form;
	
	private Idspace root;
	
	private HashMap<String, IField<?>> fields;
	
	private DBModel dbModel;
	
	private FormFieldResolver leftResolver;
	
	private FormFieldResolver rightResolver;
	
	private List<DBField> dbFields;
	

	/*package*/ FormFieldResolver(Form form) throws Exception {
		this.form = form;
		
		dbModel = new DBModel(form.getDBConnection().getConnectionString(), 
				form.getDBConnection().getClassName());
		
		root = new Idspace();
		fields = new HashMap<String, IField<?>>();
		
		if (form.isJoin()) {
			leftResolver = FormFieldResolverFactory.getResolver(form.getLeftForm());
			rightResolver = FormFieldResolverFactory.getResolver(form.getRightForm());
		}
		dbFields = findAllDBFields();
		
		HashMap<String, Object> args = new HashMap<String, Object>();
		args.put("resolver", this);
		
		Executions.createComponentsDirectly(form.getView(), null, root, args);
		readChildren(root);
	}
	
	public FormFieldResolver getLeftResolver() {
		return leftResolver;
	}
	
	public FormFieldResolver getRightResolver() {
		return rightResolver;
	}
	
	public Form getForm() {
		return form;
	}
	
	/**
	 * Returns fields
	 * @return HashMap Key - field id, value - IField object
	 */
	public HashMap<String, IField<?>> getFields() {
		return fields;
	}
	
	/**
	 * Get All DBFields of the form;
	 * @return List of DBField
	 */
	public List<DBField> getDBFields() {
		return dbFields;
	}
	
	
	/**
	 * Find field on form by DBField
	 * @param f DBField to find
	 * @return IField or null
	 */
	public IField<?> findByDBField(DBField f) {
		for(IField<?> field : fields.values()) {
			if (field.getDBField().getFullName().equals(f.getFullName()))
				return field;
		}
		return null;
	}
	
	/**
	 * Recursively find all IFields components
	 * @param c root Component
	 * @throws Exception 
	 */
	private void readChildren(Component c) throws Exception {
		List<Component> children = c.getChildren();
		if (children == null)
			return;
		
		HashMap<String, Object> args = new HashMap<String, Object>();
		args.put("resolver", this);
		
		for(Component t : children) {
			if (t instanceof IField<?>) {
				IField<?> field = (IField<?>)t;
				// onCreate event is not raised, because components are not attached to any page
				// We must call it manually
				field.onCreate(args);
				fields.put(field.getId(), field);
			}
			/** RECURSION **/
			readChildren(t);
		}
	}
	
	/**
	 * Return DBField by mapping.
	 * @param formName Form name or null when form is a basic form
	 * @param map mapping
	 * @return DBField
	 * @throws SQLException
	 * @throws ClassNotFoundException
	 */
	public DBField getFieldByMapping(String formName, String map) 
			throws SQLException, ClassNotFoundException {
		if (!form.isJoin()) {
			for(DBField f : dbFields) {
				if (f.getName().equals(map))
					return f;
			}
		} else {
			if (form.getLeftForm().getName().equals(formName)) {
				return leftResolver.getFields().get(map).getDBField();
			}
			if (form.getRightForm().getName().equals(formName)) {
				return rightResolver.getFields().get(map).getDBField();
			}
		}
		throw new IllegalArgumentException("DBField can not be found: " + formName + " " + map);
	}
	
	private List<DBField> findAllDBFields() throws SQLException {
		if (form.isJoin()) {
			List<DBField> fields = new ArrayList<DBField>();
			fields.addAll(leftResolver.getDBFields());
			fields.addAll(rightResolver.getDBFields());
			return fields;
		} else {
			return dbModel.getFields(form.getCatalog(), form.getTableName());
		}
	}
}
