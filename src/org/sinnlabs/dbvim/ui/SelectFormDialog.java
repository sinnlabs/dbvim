/**
 * 
 */
package org.sinnlabs.dbvim.ui;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sinnlabs.dbvim.config.ConfigLoader;
import org.sinnlabs.dbvim.model.Form;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

/**
 * Class represents select Form dialog window
 * @author peter.liverovsky
 *
 */
public class SelectFormDialog extends Window {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8865427587071273793L;
	
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
	
	private Form selectedForm = null;
	
	@Wire
	Listbox lstForms;
	@Wire
	Textbox txtFormName;
	
	public int getSelectedAction() { return nSelectedAction; }
	public Form getSelectedForm() { return selectedForm; }
	
	public SelectFormDialog() throws SQLException {
		super();
		Executions
			.createComponents("/components/selectformdialog.zul", this, null);
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
		setTitle("Select form");
		
		txtFormName_onChanged();
	}
	
	@Listen("onClick = #btnOK")
	public void btnOK_onClick() {
		if (lstForms.getSelectedItem() == null) {
			Messagebox.show("Select form first.", "Error", Messagebox.OK, Messagebox.EXCLAMATION);
			return;
		}
		selectedForm = lstForms.getSelectedItem().getValue();
		nSelectedAction = DD_OK;
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
	
	@Listen("onChange = #txtFormName; onOK = #txtFormName")
	public void txtFormName_onChanged() throws SQLException {
		List<Form> forms = null;
		lstForms.getItems().clear();
		if (StringUtils.isBlank(txtFormName.getText())) {
			forms = ConfigLoader.getInstance().getForms().queryForAll();
		} else {
			QueryBuilder<Form, String> qb = ConfigLoader.getInstance().getForms().queryBuilder();
			Where<Form, String> w = qb.where();
			w.like(Form.NAME_FIELD_NAME, "%"+txtFormName.getText()+"%");
			forms = ConfigLoader.getInstance().getForms().query(qb.prepare());
		}
		if (forms != null) {
			for(Form f : forms) {
				Listitem item = new Listitem();
				item.appendChild(new Listcell(f.getName()));
				Listcell type = new Listcell();
				if (f.isJoin())
					type.setLabel("Join");
				else
					type.setLabel("Basic");
				item.appendChild(type);
				item.appendChild(new Listcell(f.getDBConnection().getName()));
				item.setValue(f);
				lstForms.getItems().add(item);
			}
		}
	}
}
