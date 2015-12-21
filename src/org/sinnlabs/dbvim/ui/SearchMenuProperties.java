/**
 * 
 */
package org.sinnlabs.dbvim.ui;

import java.sql.SQLException;

import org.apache.commons.lang3.StringUtils;
import org.sinnlabs.dbvim.menu.MenuResolverFactory;
import org.sinnlabs.dbvim.model.Form;
import org.sinnlabs.dbvim.model.SearchMenu;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * Class represents Search menu properties field
 * @author peter.liverovsky
 *
 */
public class SearchMenuProperties extends Window {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4320231025138361304L;
	
	/**
	 * Cancel dialog action
	 */
	public static final int DD_CANCEL = 0;

	/**
	 * Ok dialog action
	 */
	public static final int DD_OK = 1;

	/**
	 * The selected action
	 */
	private int nSelectedAction = DD_CANCEL;

	@Wire
	protected Textbox txtName;
	
	@Wire
	protected Textbox txtFormName;
	
	@Wire
	protected Textbox txtLabelField;
	
	@Wire
	protected Textbox txtValueField;
	
	@Wire
	protected Textbox txtQualification;
	
	@Wire
	protected Button btnOK;
	
	@Wire
	protected Button btnCancel;
	
	protected SearchMenu menu;
	
	boolean isNew = false;
	
	Form form;
	
	public SearchMenuProperties(SearchMenu menu, boolean isNew) {
		super();
		this.menu = menu;
		this.isNew = isNew;
		
		Executions
			.createComponents("/components/searchmenuproperties.zul", this, null);
		Selectors.wireVariables(this, this, null);
		Selectors.wireComponents(this, this, false);
		Selectors.wireEventListeners(this, this);
		setBorder("normal");
		setMinheight(250);
		setMinwidth(300);
		setHeight("250px");
		setWidth("300px");
		setClosable(true);
		setSizable(true);
		setTitle("Search menu properties");
		
		initUI();
	}
	
	private void initUI() {
		if (!isNew) {
			txtQualification.setText(menu.getQualification());
			txtName.setText(menu.getName());
			txtName.setReadonly(true);
			txtLabelField.setText(menu.getLabelField());
			txtValueField.setText(menu.getValueField());
			if (menu.getForm() != null) {
				txtFormName.setText(menu.getForm().getName());
				form = menu.getForm();
			}
		} else {
			
		}
	}
	
	public int getSelectedAction() {
		return nSelectedAction;
	}
	
	@Listen("onClick = #btnSelectForm")
	public void btnSelectForm_onClick() throws SQLException {
		final SelectFormDialog dialog = new SelectFormDialog();
		this.getParent().appendChild(dialog);
		
		dialog.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

			@Override
			public void onEvent(Event arg0) throws Exception {
				if (dialog.getSelectedAction() == SelectFormDialog.DD_OK) {
					form = dialog.getSelectedForm();
					txtFormName.setText(form.getName());
				}
			}
			
		});
		
		dialog.doModal();
	}
	
	@Listen("onClick = #btnOK")
	public void btnOK_onClick() throws SQLException {
		if (StringUtils.isBlank(txtFormName.getText())) {
			showError("Name can not be empty.");
			return;
		}
		if (StringUtils.isBlank(txtFormName.getText()) || form == null) {
			showError("Form can not be empty.");
			return;
		}
		if (StringUtils.isBlank(txtValueField.getText())) {
			showError("Value field can not be empty.");
			return;
		}
		if (StringUtils.isBlank(txtLabelField.getText())) {
			showError("Label field can not be empty.");
			return;
		}
		if (!MenuResolverFactory.isNenuNameAvailable(txtName.getText())) {
			showError("Menu name is already taken.");
			return;
		}
		
		// Updates the menu
		menu.setForm(form);
		menu.setLabelField(txtLabelField.getText());
		menu.setValueField(txtValueField.getText());
		menu.setName(txtName.getText());
		menu.setQualification(txtQualification.getText());
		
		nSelectedAction = DD_OK;
		// Close dialog window
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
	
	private void showError(String message) {
		Messagebox.show(message, "Error", Messagebox.OK, Messagebox.ERROR);
	}
}
