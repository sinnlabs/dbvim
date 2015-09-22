/**
 * 
 */
package org.dbvim.dbuibuilder.ui;

import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang3.StringUtils;
import org.dbvim.dbuibuilder.model.User;
import org.dbvim.dbuibuilder.security.LoginProvider;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
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
 * Class represents add user dialog window
 * @author peter.liverovsky
 *
 */
public class AddUserDialog extends Window {

	/**
	 * 
	 */
	private static final long serialVersionUID = -220181534234726539L;
	
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
	
	/**
	 * Current user
	 */
	private User user;
	
	/* Wire interface components */
	@Wire
	Button btnOK;
	@Wire
	Button btnCancel;
	@Wire
	Textbox txtLogin;
	@Wire
	Textbox txtPassword;
	@Wire
	Textbox txtFullName;
	
	/**
	 * Returns user selected action
	 * 
	 */
	public int getSelectedAction() { return nSelectedAction; }
	
	/**
	 * Returns new user
	 */
	public User getUser() { return user; }

	
	public AddUserDialog() {
		// create the ui
		super();
		Executions
		.createComponents("/components/adduserdialog.zul", this, null);
		Selectors.wireVariables(this, this, null);
		Selectors.wireComponents(this, this, false);
		Selectors.wireEventListeners(this, this);
		setBorder("normal");
		setClosable(true);
		setTitle("Add new user");
		addEventListeners();
		
		user = null;
	}
	
	private boolean createUser() throws WrongValueException,
			NoSuchAlgorithmException, UnsupportedEncodingException {
		
		// validate user input
		if (StringUtils.isBlank(txtLogin.getText())) {
			Messagebox.show("Login can not be empty.");
			return false;
		}
		if (StringUtils.isBlank(txtPassword.getValue())) {
			Messagebox.show("Password can not be empty.");
			return false;
		}
		// create user object
		user = LoginProvider.createUser(txtLogin.getText(), txtPassword.getValue());
		user.setEnabled(true);
		user.setFullName(txtFullName.getText());
		return true;
	}
	
	/**
	 * Wire UI events
	 */
	private void addEventListeners() {
		final Window t = this;
		
		btnOK.addEventListener(Events.ON_CLICK,
				new EventListener<MouseEvent>() {

			@Override
			public void onEvent(MouseEvent e) throws Exception {
				// if user successfully created
				if (createUser()) {
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
}