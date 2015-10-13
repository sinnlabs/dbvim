/**
 * 
 */
package org.sinnlabs.dbvim.ui.db;
import org.zkoss.zul.impl.XulElement;

/**
 * Class represents Column for TableField
 * @author peter.liverovsky
 *
 */
public class TableColumnField extends XulElement {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4469848361012878571L;

	private String label;
	
	private String field;
	
	private boolean visible;
	
	public TableColumnField() {
		
	}
	
	public TableColumnField(String label, String field) {
		this.label = label;
		this.field = field;
	}

	/**
	 * Returns column display name
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * Sets column display name
	 * @param label Display name
	 */
	public void setLabel(String label) {
		this.label = label;
		update();
	}

	public String getField() {
		return field;
	}

	public void setField(String field) {
		this.field = field;
		update();
	}

	@Override
	public boolean isVisible() {
		return visible;
	}

	@Override
	public boolean setVisible(boolean visible) {
		boolean old = visible;
		this.visible = visible;
		update();
		return old;
	}
	
	private void update() {
		if (this.getParent() instanceof TableField) {
			((TableField)this.getParent()).updateHeaders();
		}
	}
}
