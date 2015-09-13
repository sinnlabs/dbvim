package org.dbvim.dbuibuilder.ui.events;

import org.dbvim.dbuibuilder.config.ConfigLoader;
import org.dbvim.dbuibuilder.model.DBConnection;
import org.dbvim.dbuibuilder.ui.AddConnectionDialog;
import org.dbvim.dbuibuilder.zk.model.DeveloperFactory;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zul.Treeitem;

public class DBConnectionOnDoubleClickEventListener implements
		EventListener<MouseEvent> {

	@Override
	public void onEvent(MouseEvent evnt) throws Exception {
		Treeitem item = (Treeitem) evnt.getTarget();
		final DBConnection dbc = (DBConnection) item.getValue();
		
		final AddConnectionDialog dialog = new AddConnectionDialog(dbc);
		
		DeveloperFactory.getInstance().getDesigner().appendChild(dialog);
		
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
