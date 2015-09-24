/**
 * 
 */
package org.sinnlabs.dbvim.ui.events;

import org.apache.commons.lang3.StringUtils;
import org.sinnlabs.dbvim.rules.engine.RulesEngine;
import org.sinnlabs.dbvim.rules.engine.exceptions.DisplayableRulesException;
import org.sinnlabs.dbvim.rules.engine.exceptions.RulesException;
import org.sinnlabs.dbvim.ui.DesignerTree;
import org.sinnlabs.dbvim.ui.DesignerTreeItem;
import org.sinnlabs.dbvim.zk.model.ComponentFactory;
import org.sinnlabs.dbvim.zk.model.DeveloperFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Treeitem;

/**
 * @author peter.liverovsky
 *
 */
public class TreeOnPopupEventListener implements EventListener<MouseEvent> {

	protected DesignerTree tree = null;
	
	@Override
	public void onEvent(MouseEvent evt) throws Exception {
		try
		{
			tree = DeveloperFactory.getInstance().getDesignerTree();
			if (tree == null)
				return;

			// get the currently selected Treeitem
			DesignerTreeItem itemSelected = (DesignerTreeItem) tree.getTree().getSelectedItem();
			
			if (itemSelected == null)
				return;
			
			/*** Copy Treeitem ***/
			if (evt.getTarget().getId().equals("mnuCopy"))
			{
				// copy the selected Treeitem
				copyTreeitem(itemSelected);
			}
			
			/*** Paste Treeitem ***/
			if (evt.getTarget().getId().equals("mnuPaste"))
			{
				// paste the copied Treeitem to the
				// selected target Treeitem
				pasteTreeitem(itemSelected);
			}
			
			/*** Delete Treeitem ***/
			if (evt.getTarget().getId().equals("mnuDelete"))
			{
				// paste the copied Treeitem to the
				// selected target Treeitem
				deleteTreeitem(itemSelected);
			}
			
			/*** View Properties ***/
			/*if (evt.getTarget().getId().equals("mnuViewProperties"))
			{
				// display the properties of the selected component
				displayComponentProperties(itemSelected.getId(), PropertiesWindow.VIEW_PROPERTIES);
			}*/
			
			/*** View Events ***/
			/*if (evt.getTarget().getId().equals("mnuViewEvents"))
			{
				// display the events of the selected component
				displayComponentProperties(itemSelected.getId(), PropertiesWindow.VIEW_EVENTS);
			}*/
		}
		catch (Exception e) {
			
		}
	}

	private void deleteTreeitem(DesignerTreeItem itemSelected) {
		try
		{
			/*Treeitem root = (Treeitem) tree.getTree().getItems().toArray()[0];
			
			if (itemSelected.getUuid().equals(root.getUuid()))
			{
				// display an error message and exit
				Messagebox.show("Root component cannot be deleted", "Error", Messagebox.OK, Messagebox.ERROR);
				return;
			}*/
			
			// get the corresponding canvas component
			Component selectedComponent = tree.getCorrespondingCanvasComponent(itemSelected);
			
			// remove the component from the canvas
			if (selectedComponent != null)
			{
				// dispose the component's resources first
				// ComponentFactory.disposeComponent(selectedComponent);
				
				Components.removeAllChildren(selectedComponent);
				selectedComponent.detach();
			}
			
			// remove the item from the tree
			tree.removeChild(itemSelected);
			itemSelected.detach();
			
			// set dirty flag
			DeveloperFactory.getInstance().getDesignerCanvas().setDirty(true);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Keeps in memory the Id of the selected
	 * Treeitem to be copied.
	 * @param sId The selected Treeitem's Id
	 * [source]
	 */
	private void copyTreeitem(DesignerTreeItem itemSelected) {
		
		if (itemSelected == null)
		{
			tree.setElementToPasteId("");
			return;
		}
	
		// get the copied component [source]
		Component sourceComponent = tree.getCorrespondingCanvasComponent(itemSelected);
		
		try
		{
			// run any Copy rules
			RulesEngine.applyRules(sourceComponent, RulesEngine.COPY_RULES);
		}
		catch (RulesException e)
		{
			try
			{
				if (e instanceof DisplayableRulesException)
					Messagebox.show(e.getMessage(), "Rules Exception", Messagebox.OK, Messagebox.ERROR);	
			}
			catch (Exception e1)
			{
			}
			
			return;
		}
		
		// keep the selected element's Id on the clipboard
		// and enable the 'Paste' action on the context menu
		tree.setElementToPasteId(itemSelected.getId());
		//_menu.getMenuItem(TreeitemContextMenu.ACTION_PASTE).setVisible(true);
	}
	
	/**
	 * Creates a clone of the canvas element
	 * that was recently copied and pastes it
	 * onto the currently selected element.
	 * @param sId The selected Treeitem's Id
	 * [target]
	 */
	private void pasteTreeitem(DesignerTreeItem itemSelected)
	{
		if ((StringUtils.isEmpty(tree.getElementToPasteId())) || itemSelected == null)
			return;
	
		try
		{
			Treeitem sourceItem = (Treeitem) tree.getTree().getFellow(tree.getElementToPasteId());
			// get the copied component [source]
			Component sourceComponent = tree.getCorrespondingCanvasComponent(sourceItem);
			
			// get the canvas component that corresponds to the 
			// currently selected Treeitem [target]
			Component targetComponent = tree.getCorrespondingCanvasComponent(itemSelected);
			
			if ((sourceComponent == null) || (targetComponent == null))
				return;

			// clone the source component
			Component cloneComponent = (Component) sourceComponent.clone();
			
			if (cloneComponent == null)
				return;

			// assign new Ids to all the elements contained 
			// within the cloned component
			cloneComponent = ComponentFactory.assignNewIds(cloneComponent);
			
			try
			{
				// run any Pre-Paste rules
				RulesEngine.applyRules(cloneComponent, targetComponent, RulesEngine.PRE_PASTE_RULES);
			}
			catch (RulesException e)
			{
				try
				{
					if (e instanceof DisplayableRulesException)
						Messagebox.show(e.getMessage(), "Rules Exception", Messagebox.OK, Messagebox.ERROR);	
				}
				catch (Exception e1)
				{
				}
				
				return;
			}
			
			// append the clone as a child to the target component
			targetComponent.appendChild(cloneComponent);
			
			// synchronize the tree with the canvas
			DeveloperFactory.getInstance().getSynchronizer().synchronizeTreeWithCanvas(
					DeveloperFactory.getInstance().getDesignerCanvas());
			
			// set dirty flag
			DeveloperFactory.getInstance().getDesignerCanvas().setDirty(true);
			
			// disable the 'Paste' action from the popup menu 
			// and clear the clipboard
			//_menu.getMenuItem(TreeitemContextMenu.ACTION_PASTE).setVisible(false);
			tree.setElementToPasteId("");
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}