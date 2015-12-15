/**
 * 
 */
package org.sinnlabs.dbvim.menu;

/**
 * Class represents Menu item
 * @author peter.liverovsky
 *
 */
public class MenuItem {

	private Object label;
	
	private Object value;
	
	public MenuItem(Object label, Object value) {
		this.label = label;
		this.value = value;
	}
	
	public MenuItem() {
		this(null, null);
	}
	
	public Object getLabel() { return label; }
	
	public void setLabel(Object label) { this.label = label; }
	
	public Object getValue() { return value; }
	
	public void setValue(Object value) { this.value = value; }
}
