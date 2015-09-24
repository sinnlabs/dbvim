/**
 * 
 */
package org.sinnlabs.dbvim.ui.events;

import org.sinnlabs.dbvim.rules.engine.RulesEngine;
import org.sinnlabs.dbvim.ui.DesignerCanvas;
import org.sinnlabs.dbvim.ui.DesignerTree;
import org.sinnlabs.dbvim.ui.MoveItemDialog;
import org.sinnlabs.dbvim.zk.model.ComponentFactory;
import org.sinnlabs.dbvim.zk.model.DeveloperFactory;
import org.sinnlabs.dbvim.zk.model.IDeveloperStudio;
import org.sinnlabs.dbvim.zk.model.IElementDesc;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Treeitem;

/**
 * @author peter.liverovsky
 *
 */
public class TreeOnDropEventListener implements EventListener<DropEvent> {
	
	/**
	 * Developer instance
	 */
	protected IDeveloperStudio developer = null;
	
	/**
	 * The selected Canvas component 
	 */
	protected Component selectedComponent = null;
	
	/**
	 * Designer tree component
	 */
	protected DesignerTree tree = null;
	
	/**
	 * Reference to the designer canvas
	 */
	protected DesignerCanvas canvas = null;
	
	public TreeOnDropEventListener() {
		developer = DeveloperFactory.getInstance();
	}
	
	public TreeOnDropEventListener(IDeveloperStudio ds) {
		developer = ds;
	}

	@Override
	public void onEvent(DropEvent event) throws Exception {
		developer = DeveloperFactory.getInstance();
		if (developer.getCurrentForm() == null) {
			Messagebox.show("Create or open form first.");
		}
		try {
			tree = developer.getDesignerTree();
			
			if (tree == null)
				return;
			
			canvas = developer.getDesignerCanvas();
			
			// get the selected component from the Canvas
			selectedComponent = tree.getSelectedComponent();
			
			// check the type of dragged item
			
			/*** Dragged: New component --> Target: Tree ***/
			if (event.getDragged() instanceof IElementDesc)
			{
				// insert the selected toolkit Component
				// into the canvas model
				addComponentToModel(event);
				return;
			}
			
			/*** Dragged: Treerow --> Target: Treerow ***/
			if (event.getDragged() instanceof Treeitem)
			{
				// move the dragged component to the new position
				// display the 'Move Component' dialog
				final MoveItemDialog wndMove = new MoveItemDialog();
				final DropEvent dropEvent = event;
				developer.getDesigner().appendChild(wndMove);
				
				wndMove.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

					@Override
					public void onEvent(Event evnt) throws Exception {
						// TODO Auto-generated method stub
						// get the selected move type
						int nMoveType = wndMove.getSelectedMove();
						moveComponent(dropEvent, nMoveType);
					}
				});
				// display it as modal
				wndMove.doModal();
				
				return;
			}
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Adds a component to the canvas model, as soon 
	 * as the user has dragged-and-dropped a component
	 * from the toolkit onto the Tree.
	 * @param evt The Drop event object
	 */
	protected void addComponentToModel(DropEvent evtDrop)
	{
		Component newComponent 	= null;
		Treeitem itemTarget 	= null;
		IElementDesc cmpDragged 	= null; 
		Component cmpTarget 	= null; 
		
		try
		{
			// get the dragged and target components
			cmpDragged = (IElementDesc) evtDrop.getDragged();
			
			// if target is treeitem
			if (evtDrop.getTarget() instanceof Treeitem) {
				itemTarget = (Treeitem) evtDrop.getTarget();

				// get the canvas Components that correspond to the 
				// dragged and target Treeitems respectively
				cmpTarget = tree.getCorrespondingCanvasComponent(itemTarget);
			} else { // target is tree
				cmpTarget = developer.getDesignerCanvas();
			}
			
			// if either component is null or that dragged and target
			// are the same object, exit
			if ((cmpTarget == null) || (cmpDragged == null) || 
				(cmpTarget == cmpDragged))
				return;
			
			// create a new component instance of this class
			newComponent = ComponentFactory.createComponent(cmpDragged.getElementInfo().getClassName());		
		
			if (newComponent == null)
				return;
		
			// check if the dragged component allows children
			//TODO Implement childable check
			//if (! cmpTarget.)
			//{	
				// display the exception string in an error box
			//	Messagebox.show("Target component does not allow any children...", "Error", Messagebox.OK, Messagebox.ERROR);
			//	return;
			//}
			
			// add the new component as a child
			// to the selected one
			cmpTarget.appendChild(newComponent);
			cmpTarget.invalidate(); // avoid some side effect. add By Jumper
			// apply the post creation rules of the component
			/*RulesResult result =*/
			RulesEngine.applyRules(newComponent, RulesEngine.CREATION_RULES);

			// clean up
			//result = null;
		}
		catch (Exception e)
		{
			if (newComponent != null)
				cmpTarget.removeChild(newComponent);
			
			// display the exception string in an error box
			Messagebox.show(e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);

			return;
		}
		
		// if the operation was successful, 
		// synchronize the Tree with the model
		if (itemTarget != null) {
			developer.getSynchronizer().setTargetTreeitem(itemTarget.getId());
		} else {
			developer.getSynchronizer().setTargetTreeitem("");
		}
		developer.getSynchronizer().synchronizeTreeWithCanvas(canvas);
		// set dirty flag
		developer.getDesignerCanvas().setDirty(true);
	}
	
	/**
	 * Move a component to the canvas model, as soon 
	 * as the user has dragged-and-dropped a component
	 * from the tree onto the Tree.
	 * @param evt The Drop event object
	 */
	protected void moveComponent(DropEvent evtDrop, int nMoveType)
	{
		Component cmpDragged 		= null;
		Component cmpParent		 	= null;
		Component cmpTarget 		= null;
		
		try
		{
			if (tree == null)
				return;
			
			// get the dragged and target Treeitems
			Treeitem itemDragged = (Treeitem) evtDrop.getDragged();
			
			if (evtDrop.getTarget() instanceof Treeitem) {
				Treeitem itemTarget = (Treeitem) evtDrop.getTarget();
				cmpTarget = tree.getCorrespondingCanvasComponent(itemTarget);
			} else {
				cmpTarget = developer.getDesignerCanvas();
			}
			// get the canvas Components that correspond to the 
			// dragged and target Treeitems respectively
			cmpDragged = tree.getCorrespondingCanvasComponent(itemDragged);
			
			
			
			// if either component is null or that dragged and target
			// are the same object, exit
			if ((cmpTarget == null) || (cmpDragged == null) || 
				(cmpTarget == cmpDragged))
				return;
			
			
			// if the move was cancelled, exit
			if (nMoveType == MoveItemDialog.DD_NONE)
				return;
			
			// perform a position change between the dragged 
			// and target components, based on the move type
			// that the user has selected
			
			if (nMoveType == MoveItemDialog.DD_AS_CHILD)
			{
				/*** Append as child ***/
				
				// append only if the target component allows children
				//TODO make childable check
				//if (! cmpTarget.isChildable())
				//{	
					// display the exception string in an error box
				//	Messagebox.show("Target component does not allow any children...", "Error", Messagebox.OK, Messagebox.ERROR);
				//	return;
				//}
				
				// check if the dragged component is an ancestor of
				// the target one
				if (Components.isAncestor(cmpDragged, cmpTarget))
				{
					// display the exception string in an error box
					Messagebox.show("Dragged component is an ancestor of the target component...", "Error", Messagebox.OK, Messagebox.ERROR);
					return;
				}
				
				// detach the dragged component from its parent
				// and append it as a child to the target one
				// Save component parent
				Component oldParent = cmpDragged.getParent();
				try {
					cmpDragged.detach();
					cmpTarget.appendChild(cmpDragged);
				} catch(Exception e) {
					// rollback
					cmpTarget.removeChild(cmpDragged);
					oldParent.appendChild(cmpDragged);
					throw e;
				}
			}
			else if (nMoveType == MoveItemDialog.DD_AS_PARENT)
			{
				/*** Set as parent ***/
				if (cmpTarget == developer.getDesignerCanvas()) {
					Messagebox.show("Can not set as parant of root element.");
					return;
				}
				
				// get the parent of the target component
				cmpParent = cmpTarget.getParent();
				
				// detach the dragged and target components
				// save element state
				Component oldDraggedParent = cmpDragged.getParent();
				Component oldTargetParent = cmpTarget.getParent();
				try {
					cmpDragged.detach();
					cmpTarget.detach();
				
					// append the dragged component to the target's parent
					cmpParent.appendChild(cmpDragged);
				
					// append the target component to the dragged one
					cmpDragged.appendChild(cmpTarget);	
				} catch(Exception e) {
					// return previous state
					cmpParent.removeChild(cmpDragged);
					cmpDragged.removeChild(cmpTarget);
					oldDraggedParent.appendChild(cmpDragged);
					oldTargetParent.appendChild(cmpTarget);
					throw e;
				}
			}
			else if (nMoveType == MoveItemDialog.DD_BEFORE)
			{
				/*** Insert Before ***/
				// check if the target component is the designer canvas
				if (cmpTarget == developer.getDesignerCanvas()) {
					Messagebox.show("Can not insert before root component.", "Error", Messagebox.OK, Messagebox.ERROR);
					return;
				}
				
				// check if the dragged component is an ancestor of
				// the target one
				if (Components.isAncestor(cmpDragged, cmpTarget))
				{
					// display the exception string in an error box
					Messagebox.show("Dragged component is an ancestor of the target component...", "Error", Messagebox.OK, Messagebox.ERROR);
					return;
				}
				
				// check if the user has tried to to insert the
				// dragged element before the root canvas component
				/*if (canvas.getRootComponent() == cmpTarget)
				{
					// display the exception string in an error box
					Messagebox.show("Dragged component cannot be inserted before the root window...", "Error", Messagebox.OK, Messagebox.ERROR);
					return;
				}*/
				
				// Save Dragged state
				Component oldDraggedParent = cmpDragged.getParent();
				
				// detach the dragged element and insert it before
				// the target element
				try {
					cmpDragged.detach();
					cmpTarget.getParent().insertBefore(cmpDragged, cmpTarget);
				} catch(Exception e) {
					cmpTarget.getParent().removeChild(cmpDragged);
					oldDraggedParent.appendChild(cmpDragged);
					throw e;
				}
			}
		}
		catch (Exception e)
		{
			// display the exception string in an error box
			Messagebox.show(e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
			e.printStackTrace();
			return;
		}
		
		// if the operation was successful, 
		// synchronize the Tree with the model
		developer.getSynchronizer().synchronizeTreeWithCanvas(canvas);
		// set dirty flag
		developer.getDesignerCanvas().setDirty(true);
	}
}
