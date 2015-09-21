/**
 * 
 */
package org.dbvim.dbuibuilder.zk;

import java.sql.SQLException;
import java.util.List;

import org.dbvim.dbuibuilder.config.ConfigLoader;
import org.dbvim.dbuibuilder.model.Role;
import org.dbvim.dbuibuilder.model.User;
import org.dbvim.dbuibuilder.security.LoginProvider;
import org.dbvim.dbuibuilder.ui.ChangeUserRoleDialog;
import org.dbvim.dbuibuilder.ui.RetypePasswordDialog;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * Composer for Administration Console
 * @author peter.liverovsky
 *
 */
public class AdministrationConsoleComposer extends SelectorComposer<Component> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7682651511058194761L;
	
	@Wire
	Window wndMain;

	@Wire
	Listbox lstUsers;
	
	@Wire
	Textbox txtLogin;
	
	@Wire
	Textbox txtPassword;
	
	@Wire
	Textbox txtFullName;
	
	@Wire
	Textbox txtRoles;
	
	@Wire
	Checkbox chbEnabled;
	
	User selectedUser;
	
	boolean isUserDirty = false;
	boolean isPasswordDirty = false;
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		
		// fill users list
		refreshUserList();
	}
	
	@Listen("onSelect = #lstUsers")
	public void lstUsers_onSelect() throws SQLException {
		if (isUserDirty) {
			Messagebox.show("User was modified. Save changes?", "User", 
					Messagebox.YES | Messagebox.NO
					, Messagebox.QUESTION, new EventListener<Event>() {

						@Override
						public void onEvent(Event e) throws Exception {
							if(Messagebox.ON_YES.equals(e.getName())){
								UpdateUser();
								userSelected();
							}else if(Messagebox.ON_NO.equals(e.getName())){
								userSelected();
							}
						}				
			});
			return;
		} else {
			userSelected();
		}
		
	}
	
	@Listen("onClick = #btnChangeRoles")
	public void btnChangeRoles_onClick() throws SQLException {
		if (selectedUser != null) {
			final ChangeUserRoleDialog dialog = new ChangeUserRoleDialog(selectedUser);
			wndMain.appendChild(dialog);
			dialog.setHeight("50%");
			dialog.setWidth("60%");
			
			dialog.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

				@Override
				public void onEvent(Event arg0) throws Exception {
					if (dialog.getSelectedAction() == ChangeUserRoleDialog.DD_OK) {
						refreshRoles();
					}
				}
				
			});
			
			dialog.doModal();
		}
	}
	
	private void userSelected() throws SQLException {
		User usr = lstUsers.getSelectedItem().getValue();
		if (usr!= null || usr != selectedUser) {
			selectedUser = usr;
			this.txtLogin.setValue(usr.getLogin());
			this.txtPassword.setText("password encrypted");
			this.txtFullName.setText(usr.getFullName());
			refreshRoles();
			this.chbEnabled.setChecked(usr.isEnabled());
			this.chbEnabled.invalidate();
			isUserDirty = false;
		}
	}
	
	private void refreshRoles() throws SQLException {
		List<Role> roles = selectedUser.getRoles();
		String r = "";
		for(int i=0; i<roles.size(); i++) {
			r+= roles.get(i).getName();
			if (i<roles.size()-1) {
				r+= "; ";
			}
		}
		this.txtRoles.setValue(r);
	}
	
	private void refreshUserList() throws SQLException {
		lstUsers.getItems().clear();
		for(User usr : ConfigLoader.getInstance().getUsers().queryForAll()) {
			Listitem item = new Listitem();
			Listcell cell = new Listcell();
			item.setValue(usr);
			cell.appendChild(new Label(usr.getLogin()));
			item.appendChild(cell);
			lstUsers.getItems().add(item);
		}
	}
	
	@Listen("onChange = #txtLogin, #txtPassword, #txtFullName; onCheck = #chbEnabled")
	public void onUserChange(Event event) {
		isUserDirty = true;
		if (event.getTarget().getId().equals("txtPassword"))
			isPasswordDirty = true;
	}
	
	@Listen("onClick = #btnUpdate")
	public void btnUpdate_onClick() {
		UpdateUser();
	}
	
	private void UpdateUser() {
		if (isUserDirty) {
			selectedUser.setFullName(txtFullName.getText());
			selectedUser.setEnabled(chbEnabled.isChecked());
			if (isPasswordDirty) {
				// retype password
				final RetypePasswordDialog dialog = new RetypePasswordDialog(txtPassword.getText());
				wndMain.appendChild(dialog);
				dialog.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

					@Override
					public void onEvent(Event arg0) throws Exception {
						// if password match
						if (dialog.getSelectedAction() == RetypePasswordDialog.DD_OK) {
							selectedUser = LoginProvider.updatePassword(selectedUser, 
									txtPassword.getValue());
							try {
								ConfigLoader.getInstance().getUsers().update(selectedUser);
								isUserDirty = false;
								isPasswordDirty = false;
							} catch (SQLException e) {
								Messagebox.show("Unable to save user.", "ERROR", Messagebox.OK, Messagebox.ERROR);
								e.printStackTrace();
							}

						}
					}
					
				});
				dialog.doModal();
			} else {
				try {
					ConfigLoader.getInstance().getUsers().update(selectedUser);
					isUserDirty = false;
					isPasswordDirty = false;
				} catch (SQLException e) {
					Messagebox.show("Unable to save user.", "ERROR", Messagebox.OK, Messagebox.ERROR);
					e.printStackTrace();
				}
			}
		}
	}
}
