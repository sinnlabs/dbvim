/**
 * 
 */
package org.sinnlabs.dbvim.menu;

import org.sinnlabs.dbvim.db.Value;

/**
 * Class represents SearchMenu item
 * @author peter.liverovsky
 *
 */
public class MenuItem {

	private Value<?> label;
	
	private Value<?> value;
	
	public MenuItem(Value<?> label, Value<?> value) {
		this.label = label;
		this.value = value;
	}
	
	public MenuItem() {
		this(null, null);
	}
	
	public Value<?> getLabel() { return label; }
	
	public void setLabel(Value<?> label) { this.label = label; }
	
	public Value<?> getValue() { return value; }
	
	public void setValue(Value<?> value) { this.value = value; }
}
