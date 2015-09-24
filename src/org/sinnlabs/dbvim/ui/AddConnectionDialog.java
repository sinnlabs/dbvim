/**
 * 
 */
package org.sinnlabs.dbvim.ui;

import org.sinnlabs.dbvim.model.DBConnection;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * @author peter.liverovsky
 *
 */
public class AddConnectionDialog extends Window {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2077390167503015288L;

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

	@Wire("#txtName")
	Textbox txtName;

	@Wire("#txtJDBC")
	Textbox txtJdbc;
	
	@Wire
	Textbox txtClass;

	@Wire
	Button btnOK;

	@Wire
	Button btnCancel;

	public AddConnectionDialog() {
		// create the ui
		super();
		Executions
				.createComponents("/components/addconnection.zul", this, null);
		Selectors.wireVariables(this, this, null);
		Selectors.wireComponents(this, this, false);
		Selectors.wireEventListeners(this, this);
		setBorder("normal");
		setClosable(true);
		setTitle("Add Connection");
		final Window t = this;

		btnOK.addEventListener(Events.ON_CLICK,
				new EventListener<MouseEvent>() {

					@Override
					public void onEvent(MouseEvent e) throws Exception {
						nSelectedAction = DD_OK;
						Event closeEvent = new Event(Events.ON_CLOSE, t);
						Events.postEvent(closeEvent);
						detach();
					}

				});
		
		btnCancel.addEventListener(Events.ON_CLICK, new EventListener<MouseEvent>() {

			@Override
			public void onEvent(MouseEvent arg0) throws Exception {
				nSelectedAction = DD_CANCEL;
				Event closeEvent = new Event(Events.ON_CLOSE, t);
				Events.postEvent(closeEvent);
				detach();
			}
			
		});
	}
	
	public AddConnectionDialog(DBConnection c) {
		this();
		txtName.setValue(c.getName());
		txtName.setReadonly(true);
		txtJdbc.setValue(c.getConnectionString());
		txtClass.setValue(c.getClassName());
	}

	/**
	 * Get the selected action
	 * @return DD_OK or DD_CANCEL
	 */
	public int getSelectedAction() {
		return nSelectedAction;
	}
	
	/**
	 * Get the connection name
	 * @return
	 */
	public String getName() {
		return txtName.getText();
	}
	
	/**
	 * Get the JDBC connection string
	 * @return
	 */
	public String getConnectionString() {
		return txtJdbc.getText();
	}
	
	/**
	 * Get the JDBC Class name
	 * @return
	 */
	public String getClassName() {
		return txtClass.getText();
	}
}
