/**
 * 
 */
package org.dbvim.dbuibuilder.ui;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * @author peter.liverovsky
 *
 */
public class RetypePasswordDialog extends Window {

	/**
	 * 
	 */
	private static final long serialVersionUID = 973324328773996641L;

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
	private int nSelectedAction = DD_CANCEL;
	
	private String password;
	
	@Wire
	Textbox txtPassword;
	
	@Wire
	Button btnOK;
	
	@Wire
	Button btnCancel;
	
	public int getSelectedAction() { return nSelectedAction; }
	
	public RetypePasswordDialog(String password) {
		// create the ui
		super();
		Executions
		.createComponents("/components/retypepassword.zul", this, null);
		Selectors.wireVariables(this, this, null);
		Selectors.wireComponents(this, this, false);
		Selectors.wireEventListeners(this, this);
		setBorder("normal");
		setClosable(true);
		setTitle("Retype password");
		addEventListeners();
		
		this.password = password;
	}
	
	/**
	 * Creates event listeners
	 */
	private void addEventListeners() {
		final Window t = this;
		
		btnOK.addEventListener(Events.ON_CLICK,
				new EventListener<MouseEvent>() {

			@Override
			public void onEvent(MouseEvent e) throws Exception {
				if (checkPassword()) {
					nSelectedAction = DD_OK;
					Event closeEvent = new Event(Events.ON_CLOSE, t);
					Events.postEvent(closeEvent);
					detach();
				}
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
	
	private boolean checkPassword() {
		if (!txtPassword.getText().equals(password)) {
			Messagebox.show("Password does not match.", "Password", 
					Messagebox.OK, Messagebox.EXCLAMATION);
			return false;
		}
		return true;
	}
}
