/**
 * 
 */
package org.sinnlabs.dbvim.ui;

import java.sql.SQLException;
import java.util.List;

import org.sinnlabs.dbvim.config.ConfigLoader;
import org.sinnlabs.dbvim.model.Role;
import org.sinnlabs.dbvim.model.User;
import org.sinnlabs.dbvim.model.UserRole;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Window;

import com.j256.ormlite.stmt.QueryBuilder;

/**
 * Class represents change user roles dialog window
 * @author peter.liverovsky
 *
 */
public class ChangeUserRoleDialog extends Window {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1876933663959654194L;
	
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
	Listbox lstAvailable;
	@Wire
	Listbox lstGranted;
	@Wire
	Button btnAdd;
	@Wire
	Button btnRemove;
	
	public int getSelectedAction() { return nSelectedAction; }
	
	/**
	 * Constructor
	 * @param user - User that need to be modified
	 * @throws SQLException 
	 */
	public ChangeUserRoleDialog(User user) throws SQLException {
		// create the ui
		super();
		Executions
			.createComponents("/components/ChangeUserRoles.zul", this, null);
		Selectors.wireVariables(this, this, null);
		Selectors.wireComponents(this, this, false);
		Selectors.wireEventListeners(this, this);
		setBorder("normal");
		setClosable(true);
		setTitle("Change granted roles");
		addEventListeners();
		
		this.user = user;
		
		refreshRolesList();
	}
	
	/**
	 * Populate data into role listboxes
	 * @throws SQLException
	 */
	private void refreshRolesList() throws SQLException {
		List<Role> granted = user.getRoles();
		lstGranted.getItems().clear();
		for(Role r : granted) {
			Listitem item = new Listitem();
			item.setValue(r);
			Listcell cell = new Listcell();
			cell.appendChild(new Label(r.getName()));
			item.appendChild(cell);
			lstGranted.getItems().add(item);
		}
		
		lstAvailable.getItems().clear();
		List<Role> roles = ConfigLoader.getInstance().getRoles().queryForAll();
		boolean bGranted = false;
		for(Role r : roles) {
			// Check if the role already granted
			bGranted = false;
			for(Role g : granted) {
				if (g.getName().equals(r.getName())) {
					bGranted = true;
				}
			}
			if (!bGranted) {
				Listitem item = new Listitem();
				item.setValue(r);
				Listcell cell = new Listcell();
				cell.appendChild(new Label(r.getName()));
				item.appendChild(cell);
				lstAvailable.getItems().add(item);
			}
		}
	}
	
	private void addEventListeners() {
		final Window t = this;
		
		btnOK.addEventListener(Events.ON_CLICK,
				new EventListener<MouseEvent>() {

			@Override
			public void onEvent(MouseEvent e) throws Exception {
				nSelectedAction = DD_OK;
				UpdateRoles();
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
		
		btnAdd.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

			@Override
			public void onEvent(Event arg0) throws Exception {
				if (lstAvailable.getSelectedItem() != null) {
					Listitem item = lstAvailable.getSelectedItem();
					lstAvailable.getItems().remove(item);
					lstGranted.getItems().add(item);
				}
			}
			
		});
		
		btnRemove.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

			@Override
			public void onEvent(Event arg0) throws Exception {
				if (lstGranted.getSelectedItem() != null) {
					Listitem item = lstGranted.getSelectedItem();
					lstGranted.getItems().remove(item);
					lstAvailable.getItems().add(item);
				}
			}
			
		});
	}
	
	private void UpdateRoles() throws SQLException {
		// delete all user roles
		QueryBuilder<UserRole, Integer> userRoleQb = 
				ConfigLoader.getInstance().getUserRoles().queryBuilder();
		
		userRoleQb.where().eq(UserRole.USER_ID_FIELD_NAME, user.getLogin());
		
		List<UserRole> roles = ConfigLoader.getInstance().getUserRoles().query(
				userRoleQb.prepare());
		
		ConfigLoader.getInstance().getUserRoles().delete(roles);
		
		// create new user roles
		for(Listitem i : lstGranted.getItems()) {
			Role r = (Role) i.getValue();
			UserRole ur = new UserRole(user, r);
			ConfigLoader.getInstance().getUserRoles().createIfNotExists(ur);
		}
	}
}
