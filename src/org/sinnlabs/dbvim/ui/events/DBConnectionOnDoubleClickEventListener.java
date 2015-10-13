package org.sinnlabs.dbvim.ui.events;

import org.sinnlabs.dbvim.config.ConfigLoader;
import org.sinnlabs.dbvim.model.DBConnection;
import org.sinnlabs.dbvim.ui.AddConnectionDialog;
import org.sinnlabs.dbvim.zk.model.IDeveloperStudio;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zul.Treeitem;

public class DBConnectionOnDoubleClickEventListener implements
		EventListener<MouseEvent> {
	IDeveloperStudio developer;
	
	public DBConnectionOnDoubleClickEventListener(IDeveloperStudio developer) {
		this.developer = developer;
	}

	@Override
	public void onEvent(MouseEvent evnt) throws Exception {
		Treeitem item = (Treeitem) evnt.getTarget();
		final DBConnection dbc = (DBConnection) item.getValue();
		
		final AddConnectionDialog dialog = new AddConnectionDialog(dbc);
		
		developer.getDesigner().appendChild(dialog);
		
		dialog.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

			@Override
			public void onEvent(Event arg0) throws Exception {
				if (dialog.getSelectedAction() == AddConnectionDialog.DD_OK) {
					dbc.setClassName(dialog.getClassName());
					dbc.setConnectionString(dialog.getConnectionString());
					ConfigLoader.getInstance().getDBConnections().update(dbc);
				}
			}
		});
		
		dialog.doModal();
	}
}
