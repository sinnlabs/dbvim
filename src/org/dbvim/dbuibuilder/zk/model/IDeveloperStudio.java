/**
 * 
 */
package org.dbvim.dbuibuilder.zk.model;

import org.dbvim.dbuibuilder.model.Form;
import org.dbvim.dbuibuilder.rules.engine.Rules;
import org.dbvim.dbuibuilder.ui.Designer;
import org.dbvim.dbuibuilder.ui.DesignerCanvas;
import org.dbvim.dbuibuilder.ui.DesignerEvents;
import org.dbvim.dbuibuilder.ui.DesignerProperties;
import org.dbvim.dbuibuilder.ui.DesignerTree;

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
	 * Returns the Rules object that contains all
	 * active component rules
	 * 
	 */
	public Rules getRules();

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
}
