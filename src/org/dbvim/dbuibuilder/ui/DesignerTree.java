/**
 * 
 */
package org.dbvim.dbuibuilder.ui;

import org.dbvim.dbuibuilder.ui.events.TreeOnDropEventListener;
import org.dbvim.dbuibuilder.ui.events.TreeOnPopupEventListener;
import org.dbvim.dbuibuilder.ui.events.TreeOnSelectEventListener;
import org.dbvim.dbuibuilder.zk.model.DeveloperFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Menuitem;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treechildren;
import org.zkoss.zul.Treeitem;

/**
 * UI module that displays the Treeview component
 * that shows in real time all the widgets contained
 * in a specific designer page.
 * @author peter.liverovsky
 *
 */
public class DesignerTree extends Groupbox implements IdSpace {

	public static final String TREE_ID = "designerTree";

	/**
	 * 
	 */
	private static final long serialVersionUID = 6825675684853642180L;

	/**
	 * The tree widget 
	 */
	@Wire("#trOutline")
	protected Tree tree;
	
	/**
	 * The button widget
	 */
	@Wire("#btnRefresh")
	protected Button btnRefresh;
	
	/**
	 * Context menu items
	 */
	@Wire("#mnuCopy")
	protected Menuitem mnuCopy;
	@Wire("#mnuPaste") 
	protected Menuitem mnuPaste;
	@Wire("#mnuDelete")
	protected Menuitem mnuDelete;
	

	/**
	 * The component dragged and dropped onto the Tree
	 */
	protected Component cmpDragged = null;

	/**
	 * The target component of the drag-and-drop operation 
	 */
	protected Component cmpTarget = null;

	/**
	 * The popup menu that get's displayed when the 
	 * user right-clicks on the tree
	 */
	//protected TreeitemContextMenu _menu = null;

	/**
	 * The Id of the element that the user has selected
	 * to copy-and-paste
	 */
	protected String _sElementToPasteId = "";

	// Getters / Setters
	public Tree getTree() { return tree; }
	public String getElementToPasteId() { return _sElementToPasteId; }
	public void setElementToPasteId(String sId) { _sElementToPasteId = sId; }

	public DesignerTree() {
		// create the ui
		Executions.createComponents("/components/DesignerTree.zul", this, null);
		Selectors.wireComponents(this, this, false);
		
		setTooltiptext("Real-time representation of the canvas model.");

		tree.setTooltiptext("Re-order canvas elements by dragging-and-dropping the corresponding tree items. Right-click items for context menu.");
		tree.setDroppable("true");
		setClosable(false);
		
		TreeOnPopupEventListener mnuListener = new TreeOnPopupEventListener();
		mnuCopy.addEventListener(Events.ON_CLICK, mnuListener);
		mnuPaste.addEventListener(Events.ON_CLICK, mnuListener);
		mnuDelete.addEventListener(Events.ON_CLICK, mnuListener);
		
		TreeOnSelectEventListener treeListener = new TreeOnSelectEventListener();
		tree.addEventListener(Events.ON_SELECT, treeListener);
		TreeOnDropEventListener treeOnDrop = new TreeOnDropEventListener();
		tree.addEventListener(Events.ON_DROP, treeOnDrop);
		
		btnRefresh.addEventListener(Events.ON_CLICK, new EventListener<MouseEvent>() {
			@Override
	        public void onEvent(MouseEvent e) throws Exception {
				DeveloperFactory.getInstance().getDesignerCanvas().refreshCanvas();
	        }
		});
		
		// initialize the Tree
		clearTree();
	}
	
	/**
	 * Clears the designer Tree from  
	 */
	public void clearTree()
	{
		if (tree == null)
			return;

		// clear all the Treeitems
		tree.clear();

		Treechildren children = tree.getTreechildren();

		if (children == null)
		{
			children = new Treechildren();
			tree.appendChild(children);
		}

		// create a new Window as the first Treeitem
		// to start-off the design
		//Window wndDefault = new Window();

		// first add the Window to the canvas
		//getDesigner().getCanvas().appendChild(wndDefault);

		// then add a Treeitem to the designer Tree
		//Treeitem item = getDesigner().getSynchronizer().createTreeitem(wndDefault, _tree);
		//children.appendChild(item);
	}
	
	/**
	 * Retrieves the selected Treeitem and 
	 * returns the corresponding Component
	 * from the Canvas.
	 * @return The corresponding canvas component 
	 */
	public Component getCorrespondingCanvasComponent(Treeitem item)
	{
		if (item == null)
			return null;
		
		// get the Canvas component's Id, by removig the prefix
		// 'id_' from the Treeitem Id
		String sComponentId = ((DesignerTreeItem)item).getComponentId();

		// get the corresponding Component from the canvas
		DesignerCanvas canvas = DeveloperFactory.getInstance().getDesignerCanvas();
		Component selectedComponent = canvas.getCanvasComponent(sComponentId);

		// return the component from the canvas
		return selectedComponent;		
	}
	
	/**
	 * Retrieves the selected Treeitem and 
	 * returns the corresponding Component
	 * from the Canvas.
	 * @return The corresponding canvas component 
	 */
	public Component getSelectedComponent()
	{
		// get the selected Treeitem
		Treeitem selectedItem = getTree().getSelectedItem();
					
		if (selectedItem == null)
			return null;
		
		// get the corresponding Component from the canvas
		Component selectedComponent = getCorrespondingCanvasComponent(selectedItem);

		// return the component from the canvas
		return selectedComponent;		
	}
}