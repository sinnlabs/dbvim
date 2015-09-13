/**
 * 
 */
package org.dbvim.dbuibuilder.ui;

import java.sql.SQLException;

import org.dbvim.dbuibuilder.db.model.DBField;
import org.dbvim.dbuibuilder.db.model.DBModel;
import org.dbvim.dbuibuilder.model.Form;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

/**
 * @author peter.liverovsky
 *
 */
public class SelectFieldDialog extends Window {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4538386989068396023L;

	/**
	 * The ok action
	 */
	public static final int DD_OK = 1;
	
	/**
	 * The cancel action
	 */
	public static final int DD_CANCEL = 2;
	
	public static final int CHARACTER_FIELD = 1;
	public static final int INTEGER_FIELD = 2;
	public static final int DECIMAL_FIELD = 3;
	public static final int DOUBLE_FIELD = 4;
	public static final int LONG_FIELD = 5;
	public static final int DATE_FIELD = 6;
	public static final int TIME_FIELD = 7;
	public static final int DATETIME_FIELD = 8;
	
	/**
	 * The selected action
	 */
	private int nSelectedAction = DD_CANCEL;
	
	private Form form;
	
	@Wire
	Button btnOK;
	
	@Wire
	Button btnCancel;
	
	@Wire
	Listbox lstFields;
	
	public int getSelectedAction() {return nSelectedAction; }
	
	public SelectFieldDialog(Form form, int fieldType) throws ClassNotFoundException, SQLException {
		super();
		
		this.form = form;
		
		/* create the ui */
		Executions.createComponents("/components/selectfielddialog.zul", this, null);
		Selectors.wireVariables(this, this, null);
		Selectors.wireComponents(this, this, false);
		Selectors.wireEventListeners(this, this);
		setBorder("normal");
		setClosable(false);
		setTitle("Select mapped field:");
		setWidth("50%");
		setHeight("50%");
		final Window t = this;
		
		/* add event listeners */
		btnOK.addEventListener(Events.ON_CLICK, new EventListener<MouseEvent>() {

			@Override
			public void onEvent(MouseEvent arg0) throws Exception {
				if (lstFields.getSelectedCount() != 0) {
					nSelectedAction = DD_OK;
					Event closeEvent = new Event("onClose", t);
          			Events.postEvent(closeEvent);
          			detach();
				}
				else {
					Messagebox.show("Select the field first.");
				}
			}
			
		});
		
		/* fill listbox */
		fillFields(fieldType);
	}
	
	private void fillFields(int fType) throws ClassNotFoundException, SQLException {
		DBModel model = new DBModel(form.getDBConnection().getConnectionString(), 
				form.getDBConnection().getClassName());
		
		lstFields.getItems().clear();
		
		for( DBField field : model.getFields(form.getCatalog(), form.getTableName())) {
			if (fType == convertType(field.getDBType())) {
				Listitem item = new Listitem();
				Listcell name = new Listcell();
				Listcell type = new Listcell();
				name.setLabel(field.getName());
				type.setLabel(field.getDBTypeName());
				item.setValue(field);
				item.appendChild(name);
				item.appendChild(type);
				lstFields.getItems().add(item);
			}
		}
	}
	
	private int convertType(int fType) {
		switch (fType) {
		case java.sql.Types.CHAR:
		case java.sql.Types.VARCHAR:
		case java.sql.Types.LONGVARCHAR:
		case java.sql.Types.NCHAR:
		case java.sql.Types.NVARCHAR:
		case java.sql.Types.LONGNVARCHAR:
			return CHARACTER_FIELD;
		case java.sql.Types.TINYINT:
		case java.sql.Types.SMALLINT:
		case java.sql.Types.INTEGER:
			return INTEGER_FIELD;
		case java.sql.Types.DECIMAL:
		case java.sql.Types.NUMERIC:
			return DECIMAL_FIELD;
		case java.sql.Types.REAL:
		case java.sql.Types.FLOAT:
		case java.sql.Types.DOUBLE:
			return DOUBLE_FIELD;
		case java.sql.Types.BIGINT:
			return LONG_FIELD;
		case java.sql.Types.DATE:
			return DATE_FIELD;
		case java.sql.Types.TIME:
			return TIME_FIELD;
		case java.sql.Types.TIMESTAMP:
			return DATETIME_FIELD;
		}
		return -2;
	}
	
	
	public DBField getSelectedField() {
		Listitem item = lstFields.getSelectedItem();
		
		if (item == null)
			return null;
		
		return (DBField) item.getValue();
	}
}
