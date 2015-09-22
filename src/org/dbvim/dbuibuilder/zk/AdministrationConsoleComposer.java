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
import org.dbvim.dbuibuilder.ui.AddRoleDialog;
import org.dbvim.dbuibuilder.ui.AddUserDialog;
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
	Listbox lstRoles;
	
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
	
	@Wire
	Textbox txtRoleName;
	
	@Wire
	Textbox txtRoleDesc;
	
	User selectedUser;
	Role selectedRole;
	
	boolean isUserDirty = false;
	boolean isPasswordDirty = false;
	boolean isRoleDirty = false;
	
	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		
		// fill users list
		refreshUserList();
		// fill roles list
		refreshRoleList();
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
	
	@Listen("onSelect = #lstRoles")
	public void lstRoles_onSelect() {
		if (isRoleDirty) {
			Messagebox.show("Role was modified. Save changes?", "Role", 
					Messagebox.YES | Messagebox.NO
					, Messagebox.QUESTION, new EventListener<Event>() {

						@Override
						public void onEvent(Event e) throws Exception {
							if(Messagebox.ON_YES.equals(e.getName())){
								UpdateRole();
								roleSelected();
							}else if(Messagebox.ON_NO.equals(e.getName())){
								roleSelected();
							}
						}
			});
			return;
		} else {
			roleSelected();
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
	
	private void roleSelected() {
		Role r = lstRoles.getSelectedItem().getValue();
		if (r != null || r != selectedRole) {
			selectedRole = r;
			txtRoleName.setValue(r.getName());
			txtRoleDesc.setValue(r.getDescription());
			isRoleDirty = false;
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
	
	private void refreshRoleList() throws SQLException {
		lstRoles.getItems().clear();
		for(Role r : ConfigLoader.getInstance().getRoles().queryForAll()) {
			Listitem item = new Listitem();
			Listcell cell = new Listcell();
			item.setValue(r);
			cell.appendChild(new Label(r.getName()));
			item.appendChild(cell);
			lstRoles.getItems().add(item);
		}
	}
	
	/**
	 * if user has been changed, set dirty flag
	 * @param event
	 */
	@Listen("onChange = #txtLogin, #txtPassword, #txtFullName; onCheck = #chbEnabled")
	public void onUserChange(Event event) {
		if (selectedUser == null)
			return;
		isUserDirty = true;
		if (event.getTarget().getId().equals("txtPassword"))
			isPasswordDirty = true;
	}
	
	/**
	 * if role has been changed set dirty flag
	 */
	@Listen("onChange = #txtRoleDesc, #txtRoleName")
	public void onRoleChange() {
		if (selectedRole == null)
			return;
		isRoleDirty = true;
	}
	
	@Listen("onClick = #btnUpdate")
	public void btnUpdate_onClick() {
		UpdateUser();
	}
	
	@Listen("onClick = #btnUpdateRole")
	public void btnUpdateRole_onClick() {
		UpdateRole();
	}
	
	@Listen("onClick = #btnDeleteUser")
	public void btnDeleteUser_onClick() {
		if (selectedUser == null)
			return;

		Messagebox.show("You are shure, that you want to delete user?", "User delete", 
				Messagebox.YES | Messagebox.NO
				, Messagebox.QUESTION, new EventListener<Event>() {

			@Override
			public void onEvent(Event e) throws Exception {
				if(Messagebox.ON_YES.equals(e.getName())){
					ConfigLoader.getInstance().getUsers().delete(selectedUser);
					selectedUser = null;
					refreshUserList();
				}
			}
		});
	}
	
	@Listen("onClick = #btnDeleteRole")
	public void btnDeleteRole_onClick() {
		if (selectedRole == null)
			return;
		
		Messagebox.show("You are shure, that you want to delete role?", "Role delete", 
				Messagebox.YES | Messagebox.NO
				, Messagebox.QUESTION, new EventListener<Event>() {

			@Override
			public void onEvent(Event e) throws Exception {
				if(Messagebox.ON_YES.equals(e.getName())){
					ConfigLoader.getInstance().getRoles().delete(selectedRole);
					selectedRole = null;
					refreshRoleList();
				}
			}
		});
	}
	
	@Listen("onClick = #btnAddRole")
	public void btnAddRole_onClick() {
		final AddRoleDialog dialog = new AddRoleDialog();
		wndMain.appendChild(dialog);
		
		dialog.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

			@Override
			public void onEvent(Event arg0) throws Exception {
				// ok button clicked
				if (dialog.getSelectedAction() == AddUserDialog.DD_OK) {
					Role r = dialog.getRole();
					// save new role
					ConfigLoader.getInstance().getRoles().create(r);
					// update UI
					refreshRoleList();
				}
			}
			
		});
		dialog.setWidth("50%");
		// show dialog window
		dialog.doModal();
	}
	
	@Listen("onClick = #btnAddUser")
	public void btnAddUser_onClick() {
		final AddUserDialog dialog = new AddUserDialog();
		wndMain.appendChild(dialog);
		
		dialog.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

			@Override
			public void onEvent(Event arg0) throws Exception {
				if (dialog.getSelectedAction() == AddUserDialog.DD_OK) {
					User user = dialog.getUser();
					// save new user
					ConfigLoader.getInstance().getUsers().create(user);
					// refresh UI
					refreshUserList();
				}
			}
			
		});
		dialog.setWidth("50%");
		dialog.doModal();
	}
	
	/**
	 * Updates selected role
	 */
	private void UpdateRole() {
		if (selectedRole == null)
			return;
		
		// if role has been changed
		if (isRoleDirty) {
			selectedRole.setDescription(txtRoleDesc.getText());
			try {
				ConfigLoader.getInstance().getRoles().update(selectedRole);
				isRoleDirty = false;
			} catch (SQLException e) {
				Messagebox.show("Unable to save role.", "ERROR", Messagebox.OK, Messagebox.ERROR);
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Updates user
	 */
	private void UpdateUser() {
		if (isUserDirty) {
			selectedUser.setFullName(txtFullName.getText());
			selectedUser.setEnabled(chbEnabled.isChecked());
			
			// if password has been changed
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
