/**
 * 
 */
package org.sinnlabs.dbvim.ui;

import org.sinnlabs.zk.ui.CodeMirror;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Window;

/**
 * Class represents Expand window. Used to edit multiline text components properties
 * @author peter.liverovsky
 *
 */
public class ExpandWindow extends Window {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8983575174010612577L;
	
	/**
	 * Cancel dialog action
	 */
	public static final int DD_CANCEL = 0;

	/**
	 * Ok dialog action
	 */
	public static final int DD_OK = 1;

	/**
	 * The selected action
	 */
	private int nSelectedAction = DD_CANCEL;

	@Wire
	CodeMirror textbox;

	public ExpandWindow() {
		super();
		
		this.setTitle("Expand Window");
		this.setSizable(true);
		this.setMode(Mode.OVERLAPPED);
		this.setMinheight(150);
		this.setMinwidth(150);
		this.setHeight("150px");
		this.setWidth("150px");
		this.setClosable(true);
		
		Executions
			.createComponents("/components/expandwindow.zul", this, null);
		Selectors.wireVariables(this, this, null);
		Selectors.wireComponents(this, this, false);
		Selectors.wireEventListeners(this, this);
	}
	
	public int getSelectedAction() {
		return nSelectedAction;
	}
	
	/**
	 * Sets ExpandWindow content
	 * @param txt Content
	 */
	public void setText(String txt) {
		textbox.setText(txt);
	}
	
	/**
	 * Returns ExpandWindow content
	 * @return
	 */
	public String getText() {
		return textbox.getText();
	}
	
	/**
	 * Sets codemirror mode
	 */
	public void setMode(String mode) {
		textbox.setMode(mode);
	}
	
	@Listen("onClick = #btnOK")
	public void btnOK_onClick() {
		nSelectedAction = DD_OK;
		Event closeEvent = new Event(Events.ON_CLOSE, this);
		Events.postEvent(closeEvent);
		detach();
	}
	
	@Listen("onClick = #btnCancel")
	public void btnCancel_onClick() {
		nSelectedAction = DD_CANCEL;
		Event closeEvent = new Event(Events.ON_CLOSE, this);
		Events.postEvent(closeEvent);
		detach();
	}
}
