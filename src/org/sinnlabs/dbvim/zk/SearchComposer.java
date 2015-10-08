/**
 * 
 */
package org.sinnlabs.dbvim.zk;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.sinnlabs.dbvim.db.Database;
import org.sinnlabs.dbvim.db.DatabaseJoin;
import org.sinnlabs.dbvim.db.Entry;
import org.sinnlabs.dbvim.db.Value;
import org.sinnlabs.dbvim.db.exceptions.DatabaseOperationException;
import org.sinnlabs.dbvim.db.model.IDBField;
import org.sinnlabs.dbvim.evaluator.exceptions.ParseException;
import org.sinnlabs.dbvim.form.FormFieldResolver;
import org.sinnlabs.dbvim.model.Form;
import org.sinnlabs.dbvim.model.ResultColumn;
import org.sinnlabs.dbvim.ui.IField;
import org.sinnlabs.dbvim.zk.model.CurrentForm;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Idspace;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listfooter;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.North;
import org.zkoss.zul.South;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.impl.InputElement;

/**
 * Class implements zk composer
 * 
 * @author peter.liverovsky
 *
 */
public class SearchComposer extends SelectorComposer<Component> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8674446600172735254L;
	
	protected static final int MODE_SEARCH = 1;
	
	protected static final int MODE_RESULT = 2;
	
	protected static final int MODE_CREATE = 3;

	@Wire("#border #searchResults")
	North searchResults;

	@Wire("#border #center")
	Idspace detailsView;
	
	@Wire("#border #btnSearch")
	Button btnSearch;
	
	@Wire("#lstResults")
	Listbox results;
	
	@Wire("#btnNewSearch")
	Toolbarbutton btnNewSearch;
	
	@Wire("#btnSave")
	Toolbarbutton btnSave;
	
	@Wire("#btnChangeAll")
	Toolbarbutton btnChange;
	
	@Wire("#btnCopyToNew")
	Toolbarbutton btnCopy;
	
	@Wire("#btnDelete")
	Toolbarbutton btnDelete;
	
	@Wire("#btnCreate")
	Toolbarbutton btnCreate;
	
	@Wire("#btnNewEntry")
	Toolbarbutton btnNewQuery;
	
	@Wire("#btnAdditionalSearch")
	Toolbarbutton btnAdditionalSearch;
	
	@Wire("#lstFooterTotal")
	Listfooter lstFooter;
	
	@Wire("#divSearch")
	Hlayout divSearch;
	
	@Wire("#divNewEntry")
	Hlayout divNewEntry;
	
	@Wire("#divModify")
	Hlayout divModify;
	
	@Wire("#south")
	South south;
	
	@Wire("#txtAdditionalSearch")
	Textbox txtAdditionalSearch;

	Form form;
	
	FormFieldResolver resolver;
	
	Entry currentEntry;
	
	List<Component> fieldList;
	
	List<Component> readonlyFields;
	
	Database db;
	
	List<Value<?>> lastSearch;

	private boolean isAdditional = false;
	
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);

		form = CurrentForm.getInstance().getForm();
		resolver = new FormFieldResolver(form);
		fieldList = new ArrayList<Component>();
		
		if (form.isJoin())
			db = new DatabaseJoin(form, resolver);
		else
			db = new Database(form, resolver);
		
		loadForm();
		
		currentEntry = null;
		// build search result headers
		results.getListhead().setSizable(true);
		results.setMultiple(true);
		if (form.getResultList() != null) {
			for(ResultColumn column : form.getResultList()) {
				Listheader header = new Listheader();
				header.setSort("auto");
				header.setLabel(column.label);
				results.getListhead().appendChild(header);
			}
		}
		setMode(MODE_SEARCH);
	}
	
	@Listen("onClick = #btnSearch")
	public void btnSearch_onClick() {
		List<Value<?>> userValues = getUserValues();
		lastSearch = userValues;
		search(lastSearch);
	}
	
	@Listen("onSelect = #lstResults")
	public void lstResults_onSelect() {
		Entry e = results.getSelectedItem().getValue();
		try {
			currentEntry = db.readEntry(e);
		} catch (DatabaseOperationException ex) {
			Messagebox.show("DB Operation error: " + ex.getMessage(), "ERROR",
					Messagebox.OK, Messagebox.ERROR);
			ex.printStackTrace();
			return;
		}
		populateFields();
	}
	
	@Listen("onClick = #btnNewSearch")
	public void btnNewSearch_onClick() {
		setMode(MODE_SEARCH);
		clearAllFields(detailsView);
	}
	
	@Listen("onClick = #btnAdditionalSearch")
	public void btnAdditionalSearch_onClick() {
		setAdditionalSearch(!isAdditional);
	}
	
	@Listen("onClick = #btnNewEntry")
	public void btnNewEntry_onClick() {
		setMode(MODE_CREATE);
		clearAllFields(detailsView);
	}
	
	@Listen("onClick = #btnSave")
	public void btnSave_onClick() {
		if (currentEntry != null) {
			if (scanForNulls()) {
				return;
			}
			// get values
			List<Value<?>> values = new ArrayList<Value<?>>();
			for(Component c : fieldList) {
				values.add(((IField<?>)c).getDBValue());
			}
			
			//update entry
			try {
				db.updateEntry(currentEntry, values);
			} catch (DatabaseOperationException e) {
				Messagebox.show("Unable to update entry.", "Update error.", 
						Messagebox.OK, Messagebox.ERROR);
				e.printStackTrace();
			}
		}
	}
	
	@Listen("onClick = #btnCreate")
	public void btnCreate_onClick() {
		if (scanForNulls()) {
			return;
		}
		// get values
		Entry e = new Entry();
		
		for(Component c : fieldList) {
			IField<?> f = (IField<?>) c;
			if (f.getDBValue().getValue() != null)
				e.getValues().add(f.getDBValue());
		}
		// if no user values
		if (e.getValues().size() == 0)
			return;
		// create new entry
		try {
			db.insertEntry(e);
			clearAllFields(detailsView);
		} catch (DatabaseOperationException e1) {
			Messagebox.show("Unable to create entry");
			e1.printStackTrace();
		}
	}
	
	@Listen("onClick = #btnDelete")
	public void btnDelete_onClick() {
		if (currentEntry != null) {
			Messagebox.show("You are shure that you want to delete entries?", "Delete",
					Messagebox.YES|Messagebox.NO, Messagebox.QUESTION, new EventListener<Event>() {

						@Override
						public void onEvent(Event evnt) throws Exception {
							if (evnt.getName().equals(Messagebox.ON_YES)) {
								try {
									for(Listitem i : results.getSelectedItems()) {
										Entry e = i.getValue();
										db.deleteEntry(e);
									}
								} catch (DatabaseOperationException e) {
									Messagebox.show("Unable to delte entry.", "Error", 
											Messagebox.OK, Messagebox.ERROR);
									e.printStackTrace();
								}
								search(lastSearch);
							}
						}
				
			});
			
		}
	}
	
	private void search(List<Value<?>> values) {
		List<Entry> entries = null;
		if (values == null)
			return;
		try {
			if (isAdditional) {
				List<IField<?>> fields = new ArrayList<IField<?>>();
				for(Component c : fieldList) {
					fields.add((IField<?>) c);
				}
				entries = db.query(txtAdditionalSearch.getText(), fields, 0);
			} else if (isAdditional == false && values.size() == 0) {
				entries = db.queryAll(0);
			} else
				entries = db.query(values, 0);
		} catch(DatabaseOperationException e) {
			Messagebox.show("DB Operation error: " + e.getMessage(), "ERROR",
					Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
			return;
		} catch (WrongValueException e1) {
			Messagebox.show(e1.getMessage(), "ERROR",
					Messagebox.OK, Messagebox.ERROR);
			e1.printStackTrace();
		} catch (ParseException e1) {
			Messagebox.show(e1.getMessage(), "ERROR",
					Messagebox.OK, Messagebox.ERROR);
			e1.printStackTrace();
		}
		results.getItems().clear();
		if (entries == null || entries.size() == 0) {
			Messagebox.show("No etries found.");
			return;
		}
		for(Entry e : entries) {
			Listitem item = new Listitem();
			item.setValue(e);
			for (Value<?> v : e.getValues()) {
				Listcell cell = new Listcell();
				if (v.getValue() != null)
					cell.setLabel(v.getValue().toString());
				item.appendChild(cell);
			}
			results.getItems().add(item);
		}
		lstFooter.setLabel(results.getItemCount() + " entries");
		// select first item
		if (results.getItemCount() > 0) {
			results.setSelectedIndex(0);
			currentEntry = results.getSelectedItem().getValue();
			try {
				currentEntry = db.readEntry(currentEntry);
			} catch (DatabaseOperationException e) {
				Messagebox.show("DB Operation error: " + e.getMessage(), "ERROR",
						Messagebox.OK, Messagebox.ERROR);
				e.printStackTrace();
				return;
			}
			populateFields();
		}
		setMode(MODE_RESULT);
	}
	
	private boolean scanForNulls() {
		boolean ret = false;
		for(Component c : this.fieldList) {
			IField<?> f = (IField<?>) c;
			IDBField dbf = (IDBField) c;
			if (f.getDBValue().getValue() == null && !dbf.isNullable() && !dbf.isGenerated()) {
				f.setErrorMessage("Value can not be null.");
				ret = true;
			}
		}
		return ret;
	}
	
	private void loadForm() throws Exception {
		StringReader r = new StringReader(form.getView());
		HashMap<String, Object> args = new HashMap<String, Object>();
		args.put("resolver", resolver);
		Executions.createComponentsDirectly(r, null, detailsView, args);
		Selectors.wireVariables(detailsView, this, null);
		
		// Find all DB fields of the form
		findAllDBFields(detailsView);
	}
	
	private void findAllDBFields(Component c) {
		if (c == null || c.getChildren() == null)
			return;
		for(Component child : c.getChildren()) {
			if (child instanceof IField) {
				fieldList.add(child);
			}
			/** RECURSION **/
			findAllDBFields(child);
		}
	}
	
	private void clearAllFields(Component c) {
		if (c == null || c.getChildren() == null)
			return;
		for(Component child : c.getChildren()) {
			if (child instanceof InputElement) {
				((InputElement)child).setRawValue(null);
				((InputElement)child).clearErrorMessage();
			}
			/** RECURSION **/
			clearAllFields(child);
		}
	}
	
	private List<Value<?>> getUserValues() {
		List<Value<?>> list = new ArrayList<Value<?>>();
		for (Component c : fieldList) {
			IField<?> f = (IField<?>) c;
			Value<?> v = f.getDBValue();
			if (v.getValue() != null) {
				list.add(v);
			}
		}
		return list;
	}
	
	@SuppressWarnings("unchecked")
	private void populateFields() {
		// read all fields
		for(Component c : fieldList) {
			IDBField dbField = (IDBField) c;
			IField<Object> field = (IField<Object>) c;
			for(Value<?> v : currentEntry.getValues()) {
				// find value for the field
				if (dbField.getFullName().equals(v.getDBField().getFullName())) {
					field.setDBValue((Value<Object>) v);
				}
			}
		} // for
	} // populateFields
	
	private void setMode(int mode) {
		if (mode == MODE_SEARCH) {
			searchResults.setVisible(false);
			btnSearch.setVisible(true);
			btnSave.setVisible(false);
			btnCreate.setVisible(false);
			btnChange.setDisabled(true);
			btnCopy.setDisabled(true);
			btnDelete.setDisabled(true);
			btnAdditionalSearch.setDisabled(false);
			south.setVisible(false);
			divSearch.setVisible(true);
			divNewEntry.setVisible(false);
			divModify.setVisible(false);
			setFieldsMode(IField.MODE_SEARCH);
			setAdditionalSearch(false);
		}
		if (mode == MODE_RESULT) {
			searchResults.setVisible(true);
			btnSearch.setVisible(false);
			btnSave.setVisible(true);
			btnCreate.setVisible(false);
			btnChange.setDisabled(false);
			btnCopy.setDisabled(false);
			btnDelete.setDisabled(false);
			btnAdditionalSearch.setDisabled(true);
			south.setVisible(false);
			divSearch.setVisible(false);
			divNewEntry.setVisible(false);
			divModify.setVisible(true);
			setFieldsMode(IField.MODE_MODIFY);
		}
		if (mode == MODE_CREATE) {
			searchResults.setVisible(false);
			btnSearch.setVisible(false);
			btnSave.setVisible(false);
			btnCreate.setVisible(true);
			btnChange.setDisabled(true);
			btnCopy.setDisabled(true);
			btnDelete.setDisabled(true);
			btnAdditionalSearch.setDisabled(true);
			south.setVisible(false);
			divSearch.setVisible(false);
			divNewEntry.setVisible(true);
			divModify.setVisible(false);
			if (form.isJoin())
				btnCreate.setDisabled(true);
			else
				btnCreate.setDisabled(false);
			setFieldsMode(IField.MODE_MODIFY);
		}
	}
	
	private void setAdditionalSearch(boolean b) {
		south.setVisible(b);
		isAdditional  = b;
	}
	
	private void setFieldsMode(int mode) {
		for(Component c : fieldList){
			IField<?> f = (IField<?>)c;
			f.setFieldMode(mode);
		}
	}
}