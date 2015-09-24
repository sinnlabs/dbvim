/**
 * 
 */
package org.sinnlabs.dbvim.ui;

import org.apache.commons.lang3.StringUtils;
import org.sinnlabs.dbvim.model.Role;
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
 * Class represents add role dialog window
 * @author peter.liverovsky
 *
 */
public class AddRoleDialog extends Window {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6008891809723172492L;

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
	 * Current role
	 */
	private Role role;
	
	/* Wire interface components */
	@Wire
	Button btnOK;
	@Wire
	Button btnCancel;
	@Wire
	Textbox txtName;
	@Wire
	Textbox txtDesc;
	
	/**
	 * Returns user selected action
	 */
	public int getSelectedAction() { return nSelectedAction; }
	
	/**
	 * Returns new role
	 */
	public Role getRole() { return role; }
	
	public AddRoleDialog() {
		// create the ui
		super();
		Executions
		.createComponents("/components/addroledialog.zul", this, null);
		Selectors.wireVariables(this, this, null);
		Selectors.wireComponents(this, this, false);
		Selectors.wireEventListeners(this, this);
		setBorder("normal");
		setClosable(true);
		setTitle("Add new role");
		addEventListeners();

		role = null;
	}
	
	/**
	 * Creates new role
	 * @return true if role successfully created, otherwise false.
	 */
	private boolean createRole() {
		// validate user input
		if(StringUtils.isBlank(txtName.getText())) {
			Messagebox.show("Name can not be empty.");
			return false;
		}
		// create new role
		role = new Role(txtName.getText(), txtDesc.getText());
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
				if (createRole()) {
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
