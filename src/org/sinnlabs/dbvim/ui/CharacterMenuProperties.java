/**
 * 
 */
package org.sinnlabs.dbvim.ui;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sinnlabs.dbvim.config.ConfigLoader;
import org.sinnlabs.dbvim.menu.MenuResolverFactory;
import org.sinnlabs.dbvim.model.CharacterMenu;
import org.sinnlabs.dbvim.model.CharacterMenuItem;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.WrongValueException;
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

/**
 * CharacterMenu properties dialog window
 * @author peter.liverovsky
 *
 */
public class CharacterMenuProperties extends Window {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2193250248314874319L;
	
	/**
	 * Cancel dialog action
	 */
	public static final int DD_CANCEL = 0;

	/**
	 * Create or update connection
	 */
	public static final int DD_OK = 1;

	private int nSelectedAction = DD_CANCEL;
	
	private boolean isNew = false;

	protected CharacterMenu menu;
	
	@Wire
	protected Listbox lstItems;
	
	@Wire
	protected Textbox txtLabel;
	
	@Wire
	protected Textbox txtValue;
	
	@Wire
	protected Textbox txtName;

	/*Comparator for sorting the list by Student Name*/
    public static Comparator<CharacterMenuItem> ItemComparator = new Comparator<CharacterMenuItem>() {

	public int compare(CharacterMenuItem s1, CharacterMenuItem s2) {
	   
	   //ascending order
	   return s1.getOrder() - s2.getOrder();

	   //descending order
	   //return StudentName2.compareTo(StudentName1);
    }};

	
	/**
	 * Creates character menu dialog instance
	 * @param menu CharacterMenu to change or null for creating new menu
	 */
	public CharacterMenuProperties(CharacterMenu menu) {
		super();
		// if menu is null then create a new menu instance
		if (menu == null) {
			menu = new CharacterMenu();
			menu.setName("New CharacterMenu");
			isNew = true;
		}
		this.menu = menu;
		
		// create the ui
		Executions
		.createComponents("/components/CharacterMenuProperties.zul", this, null);
		Selectors.wireVariables(this, this, null);
		Selectors.wireComponents(this, this, false);
		Selectors.wireEventListeners(this, this);
		setBorder("normal");
		setClosable(true);
		setSizable(true);
		setMinheight(200);
		setMinwidth(220);
		setTitle("CharacterMenu properties");

		// refresh items list
		refreshMenuItems();
		
		txtName.setText(menu.getName());
	}
	
	/**
	 * Refreshes menu items list
	 */
	private void refreshMenuItems() {
		lstItems.getItems().clear();
		
		List<CharacterMenuItem> items = new ArrayList<CharacterMenuItem>();
		if (menu.getItems() != null) {
			for(CharacterMenuItem i : menu.getItems()) {
				items.add(i);
			}
		}
		
		// sort items by order
		Collections.sort(items, ItemComparator);
		
		for(CharacterMenuItem i : items) {
			Listitem item = new Listitem();
			item.appendChild(new Listcell(i.getLabel()));
			item.appendChild(new Listcell(i.getValue()));
			item.setValue(i);
			lstItems.getItems().add(item);
		}
	}
	
	/**
	 * Returns user selected action
	 * @return DD_OK or DD_CANCEL
	 */
	public int getSelectedAction() {
		return nSelectedAction;
	}
	
	@Listen("onClick = #btnOK")
	public void btnOK_onClick() throws WrongValueException, SQLException {
		if (StringUtils.isBlank(txtName.getText())) {
			Messagebox.show("Name can not be empty.");
			return;
		}
		if (isNew && !MenuResolverFactory.isNenuNameAvailable(txtName.getText())) {
			Messagebox.show("Menu name is already taken.");
			return;
		}
		
		menu.setName(txtName.getText());
		
		if (isNew) {
			ConfigLoader.getInstance().getCharacterMenu().create(menu);
			for(int i=0; i<lstItems.getItemCount(); i++) {
				CharacterMenuItem item = lstItems.getItems().get(i).getValue();
				item.setOrder(i);
				ConfigLoader.getInstance().getCharacterMenuItems().create(item);
			}
		} else {
			// clear menu items
			menu.getItems().clear();
			for(int i=0; i<lstItems.getItemCount(); i++) {
				CharacterMenuItem item = lstItems.getItems().get(i).getValue();
				item.setOrder(i);
				menu.getItems().add(item);
			}
			ConfigLoader.getInstance().getCharacterMenu().update(menu);
		}
		
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
	
	@Listen("onClick = #btnAdd")
	public void btnAdd_Click() {
		CharacterMenuItem menuItem = new CharacterMenuItem();
		
		menuItem.setLabel(txtLabel.getText());
		menuItem.setValue(txtValue.getText());
		menuItem.setMenu(menu);
		
		Listitem item = new Listitem();
		item.setValue(menuItem);
		item.appendChild(new Listcell(menuItem.getLabel()));
		item.appendChild(new Listcell(menuItem.getValue()));
		
		lstItems.getItems().add(item);
	}
	
	@Listen("onClick = #btnDelete")
	public void btnDelete_onClick() {
		if (lstItems.getSelectedItem() != null) {
			lstItems.getItems().remove(lstItems.getSelectedItem());
		}
	}
	
	@Listen("onClick = #btnUp")
	public void btnUp_onClick() {
		if (lstItems.getSelectedItem() != null) {
			int index = lstItems.getSelectedIndex();
			Listitem item = lstItems.getSelectedItem();
			index--;
			if (index >= 0) {
				lstItems.getItems().remove(item);
				lstItems.getItems().add(index, item);
			}
		}
	}
	
	@Listen("onClick = #btnDown")
	public void btnDown_onClick() {
		if (lstItems.getSelectedItem() != null) {
			int index = lstItems.getSelectedIndex();
			if (index <lstItems.getItemCount()-1) {
				Listitem item = lstItems.getSelectedItem();
				lstItems.getItems().remove(item);
				index++;
				lstItems.getItems().add(index, item);
			}
		}
	}
}
