/**
 * 
 */
package org.sinnlabs.dbvim.ui;

import java.sql.SQLException;
import java.util.List;

import org.sinnlabs.dbvim.config.ConfigLoader;
import org.sinnlabs.dbvim.form.FormFieldResolver;
import org.sinnlabs.dbvim.model.Form;
import org.sinnlabs.dbvim.ui.db.TableColumnField;
import org.sinnlabs.dbvim.ui.db.TableField;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.mysql.jdbc.StringUtils;

/**
 * Class represents table field properties
 * @author peter.liverovsky
 *
 */
public class TableFieldProperties extends Window {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6972481140916070501L;
	
	/**
	 * Cancel dialog action
	 */
	public static final int DD_CANCEL = 0;

	/**
	 * Create new connection
	 */
	public static final int DD_OK = 1;

	/**
	 * The selected action
	 */
	private int nSelectedAction = AddConnectionDialog.DD_CANCEL;
	
	@Wire
	protected Listbox lstAvailableFields;
	@Wire
	protected Listbox lstTableFields;
	@Wire
	protected Button btnOK;
	@Wire
	protected Button btnCancel;
	@Wire
	protected Textbox txtFormName;
	@Wire
	protected Textbox txtQualification;
	@Wire
	protected Button btnSelectForm;
	@Wire
	protected Button btnQualification;
	@Wire
	protected Button btnAdd;
	@Wire
	protected Button btnRemove;
	
	protected TableField tableField;
	
	private FormFieldResolver resolver;
	

	/**
	 * Creates the TablField properties dialog window
	 * @param tableField
	 */
	public TableFieldProperties(TableField tableField) {
		super();
		Executions
				.createComponents("/components/TableFieldProperties.zul", this, null);
		Selectors.wireVariables(this, this, null);
		Selectors.wireComponents(this, this, false);
		Selectors.wireEventListeners(this, this);
		setBorder("normal");
		setClosable(true);
		setTitle("Table Field Properties");
		setMinwidth(400);
		setMinheight(450);
		setHeight("450px");
		setWidth("400px");
		setSizable(true);
		
		this.tableField = tableField;
		init();
	}
	
	public int getSelectedAction() {
		return nSelectedAction;
	}
	
	@Listen("onClick = #btnSelectForm")
	public void btnSelectForm_onClick() throws SQLException {
		final SelectFormDialog dialog = new SelectFormDialog();
		this.appendChild(dialog);
		
		dialog.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

			@Override
			public void onEvent(Event arg0) throws Exception {
				if (dialog.getSelectedForm() != null) {
					txtFormName.setText(dialog.getSelectedForm().getName());
					txtFormName_onChange();
				}
			}
			
		});
		
		dialog.doModal();
	}
	
	@Listen("onClick = #btnOK")
	public void btnOK_onClick() {
		nSelectedAction = DD_OK;
		Form form = null;
		try {
			form = ConfigLoader.getInstance().getForms().queryForId(txtFormName.getText());
		} catch (WrongValueException | SQLException e) {
			Messagebox.show("Unable to load form: " + txtFormName.getText(), "Error", 
					Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
			return;
		}
		if (form == null) {
			Messagebox.show("Enter a valid form name", "Error", 
					Messagebox.OK, Messagebox.ERROR);
			return;
		}
		if (lstTableFields.getItemCount() == 0) {
			Messagebox.show("Add one of the form columns to the table field.", "Error", 
					Messagebox.OK, Messagebox.ERROR);
			return;
		}
		
		try {
			tableField.setFormName(txtFormName.getText());
		} catch (Exception e) {
			Messagebox.show("Unable to set form of the TableField." + e.getMessage(), 
					"Error", Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
			return;
		}
		
		tableField.setQualification(txtQualification.getText());
		tableField.getTableColumns().clear();
		for(Listitem i : lstTableFields.getItems()) {
			tableField.getTableColumns().add((TableColumnField) i.getValue());
		}
		
		Event closeEvent = new Event(Events.ON_CLOSE, this);
		Events.postEvent(closeEvent);
		detach();
	}
	
	@Listen("onClick = #btnCancel")
	public void btnCancel_onClick() {
		nSelectedAction = DD_CANCEL;
		Event closeEvent = new Event(Events.ON_CLOSE, this);
		Events.postEvent(closeEvent);
		detach();
	}
	
	@Listen("onChange = #txtFormName")
	public void txtFormName_onChange() {
		initFormFields(true);
	}
	
	@Listen("onClick = #btnAdd")
	public void btnAdd_onClick() {
		if (lstAvailableFields.getSelectedItem() != null) {
			Listitem item = lstAvailableFields.getSelectedItem();
			lstAvailableFields.getItems().remove(item);
			lstTableFields.getItems().add(item);
		}
	}
	
	@Listen("onClick = #btnRemove")
	public void btnRemove_onClick() {
		if (lstTableFields.getSelectedItem() != null) {
			Listitem item = lstTableFields.getSelectedItem();
			lstTableFields.getItems().remove(item);
			lstAvailableFields.getItems().add(item);
		}
	}
	
	@Listen("onClick = #btnUp")
	public void btnUp_onClick() {
		if (lstTableFields.getSelectedItem() != null) {
			int index = lstTableFields.getSelectedIndex();
			Listitem item = lstTableFields.getSelectedItem();
			index--;
			if (index >= 0) {
				lstTableFields.getItems().remove(item);
				lstTableFields.getItems().add(index, item);
			}
		}
	}
	
	@Listen("onClick = #btnDown")
	public void btnDown_onClick() {
		if (lstTableFields.getSelectedItem() != null) {
			int index = lstTableFields.getSelectedIndex();
			if (index <lstTableFields.getItemCount()-1) {
				Listitem item = lstTableFields.getSelectedItem();
				lstTableFields.getItems().remove(item);
				index++;
				lstTableFields.getItems().add(index, item);
			}
		}
	}
	
	private void init() {
		// loading data
		txtFormName.setText(tableField.getFormName());
		txtQualification.setText(tableField.getQualification());
		
		// if form name is not set
		if (StringUtils.isNullOrEmpty(tableField.getFormName()))
			return;
		
		initFormFields(false);
	}
	
	private void initFormFields(boolean newForm) {
		// clear the lists
		lstAvailableFields.getItems().clear();
		lstTableFields.getItems().clear();
		Form form = null;
		try {
			form = ConfigLoader.getInstance().getForms().queryForId(txtFormName.getText());
		} catch (WrongValueException | SQLException e) {
			Messagebox.show("Unable to load form: " + txtFormName.getText(), "Error", 
					Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
			return;
		}
		
		if (form == null) {
			Messagebox.show("Enter a valid form name", "Error", 
					Messagebox.OK, Messagebox.ERROR);
			return;
		}
		
		try {
			resolver = new FormFieldResolver(form);
		} catch (Exception e) {
			Messagebox.show("Unable to load form fields: " + txtFormName.getText(), "Error", 
					Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
			return;
		}
		
		List<TableColumnField> columns = tableField.getTableColumns();
		
		for (IField<?> f : resolver.getFields().values()) {
			// skip all display only fields
			// because they do not store the values
			if (f.isDisplayOnly())
				continue;
			// create the listitem
			Listitem item = new Listitem();
			Listcell name = new Listcell(f.getId());
			Listcell label = new Listcell(f.getLabel());
			item.appendChild(name);
			item.appendChild(label);
			item.setValue(new TableColumnField(f.getLabel(), f.getId()));
			
			// if it is not a new form
			// then check if column is already on the table field
			if (!newForm) {
				if (isColumnAdded(columns, f)) {
					lstTableFields.getItems().add(item);
					continue;
				}
			}
			
			lstAvailableFields.getItems().add(item);
		}
	}
	
	/**
	 * Checks if field already added to the Table
	 * @param columns TableField columns
	 * @param field Field to be checked
	 * @return
	 */
	private boolean isColumnAdded(List<TableColumnField> columns, IField<?> field) {
		for (TableColumnField c : columns) {
			if (c.getField().equals(field.getId()))
				return true;
		}
		return false;
	}
}
