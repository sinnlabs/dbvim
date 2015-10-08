/**
 * 
 */
package org.sinnlabs.dbvim.ui;

import java.sql.SQLException;

import org.sinnlabs.dbvim.form.FormFieldResolver;
import org.sinnlabs.dbvim.model.Form;
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
public class SelectJoinFieldDialog extends Window {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6506367448011340207L;

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
	
	public class FieldInfo {
		public String formName;
		public String id;
		public FieldInfo(String formName, String id) {
			this.formName = formName;
			this.id = id;
		}
	}
	
	public SelectJoinFieldDialog(Form form, String fieldType) throws ClassNotFoundException, SQLException {
		super();
		
		if (!form.isJoin())
			throw new IllegalArgumentException("Form should be join.");
		
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

	/**
	 * @param fieldType field class name
	 * @throws  
	 */
	private void fillFields(String fieldType) {
		lstFields.getItems().clear();
		try {
			FormFieldResolver lr = new FormFieldResolver(form.getLeftForm());
			FormFieldResolver rr = new FormFieldResolver(form.getRightForm());
			addFields(lr, fieldType, form.getLeftForm().getName());
			addFields(rr, fieldType, form.getRightForm().getName());
		} catch (Exception e) {
			throw new IllegalArgumentException("Unable to get field list. " + e.getMessage(), e);
		}
	}

	/**
	 * @param resolver
	 * @param fieldType
	 */
	private void addFields(FormFieldResolver resolver, String fieldType, String formName) {
		for(IField<?> field : resolver.getFields().values()) {
			if (field.getClass().getName().equals(fieldType)) {
				Listitem item = new Listitem();
				Listcell id = new Listcell();
				Listcell form = new Listcell();
				id.setLabel(field.getId() + " (" + field.getLabel() + ")");
				form.setLabel(formName);
				item.appendChild(id);
				item.appendChild(form);
				item.setValue(new FieldInfo(formName, field.getId()));
				lstFields.getItems().add(item);
			}
		}
	}
	
	public FieldInfo getSelectedField() {
		Listitem item = lstFields.getSelectedItem();
		
		if (item == null)
			return null;
		
		return (FieldInfo) item.getValue();
	}

}
