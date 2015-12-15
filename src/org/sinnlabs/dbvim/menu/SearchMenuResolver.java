/**
 * 
 */
package org.sinnlabs.dbvim.menu;

import java.util.ArrayList;
import java.util.List;

import org.sinnlabs.dbvim.db.Database;
import org.sinnlabs.dbvim.db.DatabaseFactory;
import org.sinnlabs.dbvim.db.Entry;
import org.sinnlabs.dbvim.db.Value;
import org.sinnlabs.dbvim.db.exceptions.DatabaseOperationException;
import org.sinnlabs.dbvim.evaluator.AbstractVariableSet;
import org.sinnlabs.dbvim.evaluator.DatabaseConditionBuilder;
import org.sinnlabs.dbvim.evaluator.exceptions.ParseException;
import org.sinnlabs.dbvim.form.FormFieldResolver;
import org.sinnlabs.dbvim.form.FormFieldResolverFactory;
import org.sinnlabs.dbvim.model.SearchMenu;
import org.sinnlabs.dbvim.ui.IField;
import org.sinnlabs.dbvim.zk.model.IFormComposer;

/**
 * @author peter.liverovsky
 *
 */
public class SearchMenuResolver implements MenuResolver {
	
	private FormFieldResolver resolver;
	
	private SearchMenu menu;
	
	private Database db;
	
	private IFormComposer composer;

	public SearchMenuResolver(SearchMenu menu, IFormComposer composer) throws Exception {
		this.menu = menu;
		resolver = FormFieldResolverFactory.getResolver(menu.getForm());
		db = DatabaseFactory.createInstance(menu.getForm(), resolver);
		this.composer = composer;
	}
	
	/**
	 * Returns menu items
	 * @return
	 * @throws ParseException
	 * @throws DatabaseOperationException
	 */
	public List<MenuItem> getItems() 
			throws ParseException, DatabaseOperationException {
		
		List<MenuItem> items = new ArrayList<MenuItem>();
		if (db != null && composer != null) {
			AbstractVariableSet<Value<?>> vars = 
					DatabaseConditionBuilder.buildVariablesFromFields(composer.getFields());
			
			List<IField<?>> fields = new ArrayList<IField<?>>(2);
			fields.add(resolver.getFields().get(menu.getLabelField()));
			fields.add(resolver.getFields().get(menu.getValueField()));
			
			String query = "";
			if (menu.getQualification() != null)
				query = menu.getQualification();
			
			List<Entry> entries = db.query(fields, query, 0, vars);
			for (Entry e : entries) {
				items.add(new MenuItem(e.getValues().get(0).getValue(), 
						e.getValues().get(1).getValue()));
			}
		}
		return items;
	}
	
	/**
	 * Gets MenuItem by label
	 * @param label Label to be searched
	 * @return MenuItem
	 * @throws ParseException
	 * @throws DatabaseOperationException
	 */
	public MenuItem byLabel(Object label) throws ParseException, DatabaseOperationException {
		List<MenuItem> menuItems = this.getItems();
		
		for (MenuItem i : menuItems) {
			if (i.getLabel().equals(label)) {
				return i;
			}
		}
		return null;
	}
	
	/**
	 * Gets MenuItem by Value
	 * @param value Value to be searched
	 * @return MenuItem
	 * @throws ParseException
	 * @throws DatabaseOperationException
	 */
	public MenuItem byValue(Object value) throws ParseException, DatabaseOperationException {
		List<MenuItem> menuItems = this.getItems();
		
		for (MenuItem i : menuItems) {
			if (i.getValue().equals(value)) {
				return i;
			}
		}
		return null;
	}
}
