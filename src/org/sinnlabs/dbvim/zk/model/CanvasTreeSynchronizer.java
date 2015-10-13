/**
 * 
 */
package org.sinnlabs.dbvim.zk.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.sinnlabs.dbvim.config.Configurator;
import org.sinnlabs.dbvim.rules.engine.RulesEngine;
import org.sinnlabs.dbvim.ui.DesignerCanvas;
import org.sinnlabs.dbvim.ui.DesignerTree;
import org.sinnlabs.dbvim.ui.DesignerTreeItem;
import org.sinnlabs.dbvim.ui.events.TreeOnDropEventListener;
import org.sinnlabs.dbvim.zk.IDisposable;
import org.zkoss.idom.Element;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Tree;
import org.zkoss.zul.Treecell;
import org.zkoss.zul.Treechildren;
import org.zkoss.zul.Treeitem;
import org.zkoss.zul.Treerow;

/**
 * Class that performs synchronization
 * between the model displayed on the 
 * designer Canvas and the designer Tree.
 * 
 * @author peter.liverovsky
 *
 */
public class CanvasTreeSynchronizer implements IDisposable {

	/**
	 * The Tree level that is being processed  
	 */
	private int nCurrentLevel = 0;

	/**
	 *  
	 */
	private HashMap<String, Treechildren> mapTreechildren = null;

	/**
	 * Drag-and-drop target Treeitem  
	 */
	private String targetTreeitemId = "";
	
	/**
	 * DeveloperStudio instance
	 */
	private IDeveloperStudio developer = null;

	// Getters / Setters
	public void setTargetTreeitem(String targetTreeitemId) { this.targetTreeitemId = targetTreeitemId; }
	public String getTargetTreeitem() { return targetTreeitemId; }

	/**
	 * Default constructor 
	 */
	public CanvasTreeSynchronizer(IDeveloperStudio developer)
	{
		this.developer = developer;
	}

	/**
	 * Synchronizes the Tree with the component model 
	 * that is displayed on the canvas
	 * @param root The root component of the Canvas
	 */
	public void synchronizeTreeWithCanvas(DesignerCanvas wndCanvas)
	{
		if ((wndCanvas == null))
			return;

		// get the Tree window
		DesignerTree wndTree = developer.getDesignerTree();

		if (wndTree == null)
			return;

		// get the Tree component
		Tree tree = wndTree.getTree();

		// remove all Tree items
		tree.clear();

		// reset indices
		nCurrentLevel 	= 0;

		if (mapTreechildren != null)
		{
			mapTreechildren.clear();
			mapTreechildren = null;
		}

		// set the canvas window height to 100%
		// in order to accommodate smoothly all the
		// components
		wndCanvas.setHeight("100%");

		// parse the Component model displayed on the canvas
		// and display it on the Tree
		parseComponentModel(wndCanvas, tree);
	}
	
	/**
	 * Parses input Component model that is
	 * currently displayed on the designer canvas
	 * and displays each discreet UI component as 
	 * a Tree item on the designer Tree.
	 * @param wndCanvas designer Canvas
	 * @param tree designer Tree
	 */
	protected void parseComponentModel(Component canvasComponent, 
									   Tree tree)
	{
		if ((canvasComponent == null) || (tree == null))
			return;
		
		if (mapTreechildren == null)
			mapTreechildren = new HashMap<String, Treechildren>();
		
		// if component still has an auto-Id assigned fix it
		//canvasComponent.setId(ComponentFactory.fixAutoId(canvasComponent.getId()));

		// check if component's children should be displayed
		// onto the model treeview. If not, exit now
		if (! RulesEngine.getComponentFlag(canvasComponent, RulesEngine.FLAG_SHOW_CHILDREN))
		{
			// decrease the current Tree level
			if (nCurrentLevel > 0)
				nCurrentLevel--;
			
			return;
		}
		
		// get component's children
		List<?> listChildren = canvasComponent.getChildren();

		if ((listChildren == null) || (listChildren.size() == 0))
		{
			// decrease current Tree level
			if (nCurrentLevel > 0)
				nCurrentLevel--;
			
			return;
		}
		
		// loop through all component's children
		Iterator<?> iter = listChildren.iterator();
		while (iter.hasNext())
		{
			// get next component in the list
			Component child = (Component) iter.next();
			
			if (child == null)
				continue;

			// if component still has an auto-Id assigned fix it
			// child.setId(ComponentFactory.fixAutoId(child.getUuid()));
			
			// add Component to the designer Tree
			addComponentToTree(tree, child);
			
			// increase current Tree level
			nCurrentLevel++;
			
			/*** RECURSION ***/
			parseComponentModel(child, tree);
		}
		
		// decrease current Tree level
		if (nCurrentLevel > 0)
			nCurrentLevel--;
	}

	/**
	 * Adds a canvas Component description on the
	 * designer Tree
	 * @param tree 
	 * @param canvasComponent
	 * @param parent
	 */
	public void addComponentToTree(Tree tree,
			   			           Component canvasComponent)
	{
		if ((tree == null) || (canvasComponent == null))
			return;

		try
		{
			// create a new Treeitem based on the 
			// specified canvas Component's properties
			Treeitem item = createTreeitem(canvasComponent, tree);
			
			if (item == null)
				return;
			
			// get parent Treeitems collection from the Tree
			Collection<Treeitem> clItems = tree.getItems();
			
			// get parent Treeitem from the Treeitem collection
			Treechildren treeChildren = null;
		
			// format key for the map
			String sKey = canvasComponent.getUuid() + "_" + String.valueOf(nCurrentLevel);
			
			// if component has any children, 
			// create and attach to it a Treechildren 
			// object and keep a reference to it

		    // add Treechildren object to the Hashmap
			// and use as a key to the current Tree level
			// (but only if it doesn't exist in the map)
			if (mapTreechildren.get(sKey) == null)
			{
				if (canvasComponent.getChildren().size() > 0)
				{
					treeChildren = new Treechildren();
					item.appendChild(treeChildren);
				
					mapTreechildren.put(sKey, treeChildren);
				}
			}
			
			// if Tree size is 0, then...
			if (clItems.size() == 0)
			{
				// if size is 0, we have to add the item to 
				// the ROOT Treechildren collection
				treeChildren = (Treechildren) tree.getTreechildren();
				
				if (treeChildren != null)
					treeChildren.appendChild(item);
				
				// add Treechildren object to the map
				if (mapTreechildren.get(sKey) == null)
					mapTreechildren.put(sKey, treeChildren);
			}
			else
			{
				// get Treechildren object where this item should
				// be appended to
				
				// get Treechildren object from the map that
				// is attached to the component's parent
				sKey = canvasComponent.getParent().getUuid() + "_" + String.valueOf(nCurrentLevel - 1);
				treeChildren = (Treechildren) mapTreechildren.get(sKey);
				
				// append child Treeitem to its parent
				if (treeChildren != null)
					treeChildren.appendChild(item);
				else {
					treeChildren = (Treechildren) tree.getTreechildren();
					if (treeChildren != null)
						treeChildren.appendChild(item);
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * Creates a new Treeitem based on properties
	 * of the given canvas Component.
	 * @param canvasComponent Specified Canvas Component
	 * @return new Treeitem
	 */
	public Treeitem createTreeitem(Component canvasComponent, 
								   Tree tree)
	{
		try
		{
			// create a new Treeitem to be appended to 
			// the designer Tree
			DesignerTreeItem item = new DesignerTreeItem(canvasComponent.getUuid());
			
			// set the item's Id, by using the Component's
			// Id plus prefix 'id_'
			item.setId("id_" + canvasComponent.getUuid());
			
			// create a new Treerow that will contain 
			// the Component's Id with a small-scale image
			// of the component's type
			Treerow row = new Treerow();
			item.appendChild(row);
			
			// create Id cell 
			Treecell cell = new Treecell();
			row.appendChild(cell);
			Label lbl = new Label(canvasComponent.getId() + " [" + ComponentFactory.getSimpleClassName(canvasComponent) + "]");
			cell.appendChild(lbl);
			item.setDraggable("true");
			item.setDroppable("true");
			item.addEventListener(Events.ON_DROP, new TreeOnDropEventListener(developer));
			//row.setDraggable("treeItem");
			//row.setDroppable("treeItem, toolkitComponent");
			//TODO: tree event listener
			//row.addEventListener("onDrop", new TreeEventListener());
			
			// create image cell
			Treecell cell2 = new Treecell();
			
			/*** Try to load the 16x16 image from the XML configuration file ***/
			/***                                                             ***/ 
			
			Image img16x16 = null;
			
			// get active configurator instance
			Configurator config = null; /* DesignerToolkit.getComponentsConfigurator(); */
			
			if (config != null)
			{
				// get iDOM description of the component, using the class
				// name as a filter
				Element domComponent = config.getElement("class", canvasComponent.getClass().getName(), null);
				
				if (domComponent != null)
				{
					// retrieve 16x16 image URL
					Element domImage16 = config.getElement("image16", (Element) domComponent.getParent());
				
					// create image from the specified URL source
					//if (domImage16 != null)
						//img16x16 = ComponentFactory.createImage(canvasComponent.getClass().getName(),
						//		                                domImage16.getText());
				}
			}

			if (img16x16 == null)
			{
				// if the image couldn't be created, assign the
				// component 'Unknown' image
				//img16x16 = ComponentFactory.createImage(getClass().getName(), 
				//								        "images/designer/components/unknown16.png");				
			}
			
			if (img16x16 != null)
				cell2.appendChild(img16x16);
			
			/***                                                             ***/
			/*** Try to load the 16x16 image from the XML configuration file ***/
			
			// row.appendChild(cell2);

			// return the new Treeitem
			return item;
		}
		catch (Exception e)
		{
			
		}
		
		return null;
	}



	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
