/**
 * 
 */
package org.sinnlabs.dbvim.zk.model;

import org.sinnlabs.dbvim.model.Form;
import org.sinnlabs.dbvim.ui.Designer;
import org.sinnlabs.dbvim.ui.DesignerCanvas;
import org.sinnlabs.dbvim.ui.DesignerEvents;
import org.sinnlabs.dbvim.ui.DesignerProperties;
import org.sinnlabs.dbvim.ui.DesignerTree;

/**
 * Interface for main developer window
 * 
 * @author peter.liverovsky
 *
 */
public interface IDeveloperStudio {

	/**
	 * Get the designer tree component
	 * @return DesignerTree instance
	 */
	public DesignerTree getDesignerTree();

	/**
	 * Returns the DesignerProperties object
	 * 
	 */
	public DesignerProperties getDesignerProperties();
	
	/**
	 * Return the DesignerEvents object that represents component events
	 *
	 */
	public DesignerEvents getDesignerEvents();
	
	/**
	 * Returns the DesignerCanvas instance
	 * 
	 */
	public DesignerCanvas getDesignerCanvas();

	/**
	 * Returns the Designer window
	 * 
	 */
	public Designer getDesigner();

	/**
	 * Get the Canvas tree synchronizer
	 * 
	 */
	public CanvasTreeSynchronizer getSynchronizer();
	
	/**
	 * Get the current form
	 */
	public Form getCurrentForm();
	
	/**
	 * Raise when user double clicks on form tree node
	 * @param form - target form
	 */
	public void FormTreeNode_onDoubleClick(Form form);
	
	/**
	 * Raise when user double clicks on menu tree node
	 * @param menu - target menu
	 */
	public void MenuTreeNode_onDoubleClick(Object menu);
}
