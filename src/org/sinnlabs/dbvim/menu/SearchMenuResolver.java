/**
 * 
 */
package org.sinnlabs.dbvim.menu;

import java.util.ArrayList;
import java.util.List;

import org.sinnlabs.dbvim.db.Database;
import org.sinnlabs.dbvim.db.DatabaseJoin;
import org.sinnlabs.dbvim.db.Entry;
import org.sinnlabs.dbvim.db.Value;
import org.sinnlabs.dbvim.db.exceptions.DatabaseOperationException;
import org.sinnlabs.dbvim.evaluator.AbstractVariableSet;
import org.sinnlabs.dbvim.evaluator.DatabaseConditionBuilder;
import org.sinnlabs.dbvim.evaluator.exceptions.ParseException;
import org.sinnlabs.dbvim.form.FormFieldResolver;
import org.sinnlabs.dbvim.model.SearchMenu;
import org.sinnlabs.dbvim.ui.IField;
import org.sinnlabs.dbvim.zk.model.IFormComposer;

/**
 * @author peter.liverovsky
 *
 */
public class SearchMenuResolver {
	
	private FormFieldResolver resolver;
	
	private SearchMenu menu;
	
	private Database db;

	public SearchMenuResolver(SearchMenu menu) throws Exception {
		this.menu = menu;
		resolver = new FormFieldResolver(menu.getForm());
		if (menu.getForm().isJoin()) {
			db = new DatabaseJoin(menu.getForm(), resolver);
		} else {
			db = new Database(menu.getForm(), resolver);
		}
	}
	
	public List<MenuItem> getItems(IFormComposer composer) 
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
				items.add(new MenuItem(e.getValues().get(0), e.getValues().get(1)));
			}
		}
		return items;
	}
}
