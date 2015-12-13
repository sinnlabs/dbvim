/**
 * 
 */
package org.sinnlabs.dbvim.ui.db;

import org.sinnlabs.dbvim.ui.IField;
import org.zkoss.zul.Menuitem;

/**
 * Class represents Additional search menu item
 * @author peter.liverovsky
 *
 */
public class ConditionFieldMenuitem extends Menuitem {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1725005175509730436L;
	
	protected IField<?> comp;

	public ConditionFieldMenuitem(IField<?> c) {
		comp = c;
	}
	
	public IField<?> getField() {
		return comp;
	}
}
