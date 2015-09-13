/**
 * 
 */
package org.dbvim.dbuibuilder.zk;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.dbvim.dbuibuilder.db.Database;
import org.dbvim.dbuibuilder.db.Entry;
import org.dbvim.dbuibuilder.db.Value;
import org.dbvim.dbuibuilder.db.exceptions.DatabaseOperationException;
import org.dbvim.dbuibuilder.db.model.IDBField;
import org.dbvim.dbuibuilder.model.Form;
import org.dbvim.dbuibuilder.ui.IField;
import org.dbvim.dbuibuilder.zk.model.CurrentForm;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Idspace;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listfooter;
import org.zkoss.zul.Listheader;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.North;
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
	
	@Wire("#lstFooterTotal")
	Listfooter lstFooter;

	Form form;
	
	Entry currentEntry;
	
	List<Component> fieldList;
	
	Database db;
	
	List<Value<?>> lastSearch;
	
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);

		form = CurrentForm.getInstance().getForm();
		fieldList = new ArrayList<Component>();
		db = new Database(form);
		
		loadForm();
		
		currentEntry = null;
		// build search result headers
		results.getListhead().setSizable(true);
		if (form.getResultList() != null) {
			for(String fname : form.getResultList()) {
				Listheader header = new Listheader();
				header.setSort("auto");
				header.setLabel(fname);
				results.getListhead().appendChild(header);
			}
		}
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
			try {
				db.deleteEntry(currentEntry);
			} catch (DatabaseOperationException e) {
				Messagebox.show("Unable to delte entry.", "Error", 
						Messagebox.OK, Messagebox.ERROR);
				e.printStackTrace();
			}
			search(lastSearch);
		}
	}
	
	private void search(List<Value<?>> values) {
		List<Entry> entries = null;
		if (values == null)
			return;
		
		try {
			if (values.size() == 0)
				entries = db.queryAll();
			else
				entries = db.query(values);
		} catch(DatabaseOperationException e) {
			Messagebox.show("DB Operation error: " + e.getMessage(), "ERROR",
					Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
			return;
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
	
	private void loadForm() throws IOException {
		StringReader r = new StringReader(CurrentForm.getInstance().getForm().getView());
		Executions.createComponentsDirectly(r,
				 null, detailsView, null);
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
				if (dbField.getName().equals(v.getDBField().getName())) {
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
		}
		if (mode == MODE_RESULT) {
			searchResults.setVisible(true);
			btnSearch.setVisible(false);
			btnSave.setVisible(true);
			btnCreate.setVisible(false);
			btnChange.setDisabled(false);
			btnCopy.setDisabled(false);
			btnDelete.setDisabled(false);
		}
		if (mode == MODE_CREATE) {
			searchResults.setVisible(false);
			btnSearch.setVisible(false);
			btnSave.setVisible(false);
			btnCreate.setVisible(true);
			btnChange.setDisabled(true);
			btnCopy.setDisabled(true);
			btnDelete.setDisabled(true);
		}
	}
}