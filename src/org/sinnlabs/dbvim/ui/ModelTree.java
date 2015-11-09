/**
 * 
 */
package org.sinnlabs.dbvim.ui;

import java.sql.SQLException;

import org.sinnlabs.dbvim.config.ConfigLoader;
import org.sinnlabs.dbvim.model.DBConnection;
import org.sinnlabs.dbvim.model.Form;
import org.sinnlabs.dbvim.ui.modeltree.FormTreeNode;
import org.sinnlabs.dbvim.ui.modeltree.ModelTreeNode;
import org.sinnlabs.dbvim.ui.modeltree.ModelTreeRenderer;
import org.sinnlabs.dbvim.ui.modeltree.TableTreeNode;
import org.sinnlabs.dbvim.zk.model.DeveloperFactory;
import org.sinnlabs.dbvim.zk.model.IDeveloperStudio;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Idspace;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Toolbarbutton;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treeitem;

/**
 * Class represents tree that contains all server objects (forms, menus, etc)
 * @author peter.liverovsky
 *
 */
public class ModelTree extends Idspace {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5752242522968789687L;
	
	@Wire
	protected Tree trModelTree;

	@Wire
	protected Button btnAddConnection;
	
	@Wire
	protected Toolbarbutton btnRefresh;
	
	@Wire
	protected Toolbarbutton btnDelete;

	public ModelTree() throws SQLException {

		IDeveloperStudio developer = DeveloperFactory.getInstance();
		/* create the ui */
		Executions.createComponents("/components/modeltree.zul", this, null);
		Selectors.wireComponents(this, this, false);

		this.trModelTree.clear();

		this.trModelTree.setItemRenderer(new ModelTreeRenderer(developer));
		this.trModelTree.setModel(new ModelTreeNode());

		addEventListeners();
	}

	private void btnAddConnection_Click() {
		final AddConnectionDialog dialog = new AddConnectionDialog();

		dialog.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

			@Override
			public void onEvent(Event arg0) throws Exception {
				if (dialog.getSelectedAction() == AddConnectionDialog.DD_OK) {
					DBConnection conn = new DBConnection(dialog.getName(),
							dialog.getConnectionString(), dialog.getClassName());
					ConfigLoader.getInstance().getDBConnections().create(conn);
					RefreshTree();
				}
			}

		});
		
		this.appendChild(dialog);
		dialog.doModal();
	}
	
	private void RefreshTree() throws SQLException {
		trModelTree.clear();
		trModelTree.setModel(new ModelTreeNode());
	}
	
	/**
	 * Get the selected table
	 * @return The selected table or null
	 */
	public TableTreeNode getSelectedTable() {
		Treeitem item = trModelTree.getSelectedItem();
		if (item == null)
			return null;
		
		Object val = item.getValue();
		if (val == null)
			return null;
		
		if(val instanceof TableTreeNode) {
			return (TableTreeNode) val;
		}
		return null;
	}
	
	private void addEventListeners() {
		btnAddConnection.addEventListener(Events.ON_CLICK,
				new EventListener<MouseEvent>() {

					@Override
					public void onEvent(MouseEvent e) throws Exception {
						btnAddConnection_Click();
					}

				});
		
		/* refresh tree model */
		btnRefresh.addEventListener(Events.ON_CLICK, new EventListener<MouseEvent>() {

			@Override
			public void onEvent(MouseEvent e) throws Exception {
				RefreshTree();
			}
			
		});
		
		/* Delete selected tree item */
		btnDelete.addEventListener(Events.ON_CLICK, new EventListener<MouseEvent>() {

			@Override
			public void onEvent(MouseEvent e) throws Exception {
				final Treeitem selected = trModelTree.getSelectedItem();
				if (selected != null) {
					Messagebox.show("Do you really want to delete object?", "Delete object", 
							Messagebox.YES | Messagebox.NO, 
							Messagebox.QUESTION, new EventListener<Event>() {

						@Override
						public void onEvent(Event evnt) throws Exception {
							if (Messagebox.ON_YES.equals(evnt.getName())) {
								Object value = selected.getValue();
								// Check selected item type
								if (value instanceof DBConnection) {
									ConfigLoader.getInstance().getDBConnections()
										.delete((DBConnection) value);
									RefreshTree();
								}
								if (value instanceof FormTreeNode) {
									Form frm = (Form) ((FormTreeNode)value).getForm();
									ConfigLoader.getInstance().getForms()
										.delete(frm);
									RefreshTree();
								}
							}
						}
					});

				} // if
			}
			
		});
	}
}
