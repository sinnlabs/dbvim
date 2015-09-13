/**
 * 
 */
package org.dbvim.dbuibuilder.ui;

import org.dbvim.dbuibuilder.zk.model.ElementInfo;
import org.dbvim.dbuibuilder.zk.model.IElementDesc;
import org.zkoss.zul.Listitem;

/**
 * @author peter.liverovsky
 *
 */
public class ComponentListItem extends Listitem implements IElementDesc {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5266077577930648717L;
	
	protected ElementInfo componentInfo = null;
	
	public ComponentListItem(ElementInfo info) {
		super();
		componentInfo = info;
		setLabel(info.getName());
		setTooltiptext(info.getHelpText());
		setDraggable("true");
		setDroppable("false");
	}

	@Override
	public ElementInfo getElementInfo() {
		return componentInfo;
	}
	

}
