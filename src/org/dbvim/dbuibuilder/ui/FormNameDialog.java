/**
 * 
 */
package org.dbvim.dbuibuilder.ui;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * @author peter.liverovsky
 *
 */
public class FormNameDialog extends Window {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8597870870382427612L;

	@Wire
	protected Button btnOK;
	
	@Wire
	protected Textbox txtName;
	
	public FormNameDialog(String name) {
		super();
		
		/* create the ui */
		Executions.createComponents("/components/formname.zul", this, null);
		Selectors.wireVariables(this, this, null);
		Selectors.wireComponents(this, this, false);
		Selectors.wireEventListeners(this, this);
		setBorder("normal");
		setWidth("50%");
		setClosable(false);
		setTitle("Form name");
		txtName.setText(name);
		
		final Window t = this;
		
		btnOK.addEventListener(Events.ON_CLICK,
				new EventListener<MouseEvent>() {

					@Override
					public void onEvent(MouseEvent e) throws Exception {
						Event closeEvent = new Event(Events.ON_CLOSE, t);
						Events.postEvent(closeEvent);
						detach();
					}

				});
	}
	
	public String getName() {
		return txtName.getText();
	}
}
