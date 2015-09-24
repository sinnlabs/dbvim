/**
 * 
 */
package org.sinnlabs.dbvim.ui;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Window;

/**
 * @author peter.liverovky
 *
 */
public class MoveItemDialog extends Window {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4189165511556834122L;
	
	/*** Drag-and-drop Positions ***/
	
	/**
	 * Do nothing 
	 */
	public static final int DD_NONE		= 0;
	
	/**
	 * Append the component as a child to the target 
	 */
	public static final int DD_AS_CHILD		= 1;
			
	/**
	 * Append the target as a child to the component 
	 */
	public static final int DD_AS_PARENT 	= 2;
	
	/**
	 * Insert the component before the  
	 */
	public static final int DD_BEFORE	 	= 3;
	
	/**
	 * Insert the component after the target 
	 */
	public static final int DD_AFTER	 	= 4;
	
	/**
	 * The selected move  
	 */
	private int nSelectedMove = MoveItemDialog.DD_NONE;
	
	
	@Wire("#btnAsChild")
	protected Button btnAsChild;
	
	@Wire
	protected Button btnAsParent;
	
	@Wire
	protected Button btnBefore;
	
	@Wire
	protected Button btnAfter;
	
	public MoveItemDialog() {
		// create the ui
		super();
		Executions.createComponents("/components/MoveItemDialog.zul", this, null);
		Selectors.wireVariables(this, this, null);
		Selectors.wireComponents(this, this, false);
		Selectors.wireEventListeners(this, this);
		setBorder("normal");
		//setMode("modal");
		setClosable(true);
		setTitle("Move Item");
		final Window t = this;
		btnAsChild.addEventListener("onClick", new EventListener<MouseEvent>() {
      		 public void onEvent(MouseEvent event) throws Exception {
           		 nSelectedMove = DD_AS_CHILD;
           		 Event closeEvent = new Event("onClose", t);
           		 Events.postEvent(closeEvent);
           		 detach();
      		 }
		});
		btnAsParent.addEventListener("onClick", new EventListener<MouseEvent>() {
     		 public void onEvent(MouseEvent event) throws Exception {
          		 nSelectedMove = DD_AS_PARENT;
          		 Event closeEvent = new Event("onClose", t);
          		 Events.postEvent(closeEvent);
          		 detach();
     		 }
		});
		btnBefore.addEventListener("onClick", new EventListener<MouseEvent>() {
     		 public void onEvent(MouseEvent event) throws Exception {
          		 nSelectedMove = DD_BEFORE;
          		 Event closeEvent = new Event("onClose", t);
          		 Events.postEvent(closeEvent);
          		 detach();
     		 }
		});
	}

	public int getSelectedMove() {
		return nSelectedMove;
	}
}
