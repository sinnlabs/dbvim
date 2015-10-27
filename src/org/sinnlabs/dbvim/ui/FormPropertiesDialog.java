/**
 * 
 */
package org.sinnlabs.dbvim.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.sinnlabs.dbvim.db.Value;
import org.sinnlabs.dbvim.db.model.DBField;
import org.sinnlabs.dbvim.evaluator.DatabaseConditionBuilder;
import org.sinnlabs.dbvim.form.FormFieldResolver;
import org.sinnlabs.dbvim.model.Form;
import org.sinnlabs.dbvim.model.ResultColumn;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Checkbox;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

/**
 * Class represents form properties dialog window
 * @author peter.liverovsky
 *
 */
public class FormPropertiesDialog extends Window {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4348225150602100111L;
	
	@Wire
	Textbox txtFormName;
	
	@Wire
	Listbox lstAvailableFields;
	
	@Wire
	Listbox lstResultFields;
	
	@Wire
	Button btnAdd;
	
	@Wire
	Button btnRemove;
	
	@Wire
	Button btnOK;
	
	@Wire
	Button btnCancel;
	
	@Wire
	Button btnUpdateColumnLabel;
	
	@Wire
	Textbox txtColumnLabel;
	
	@Wire
	Tab tabJoin;
	
	@Wire
	Listbox lstFirstFields;
	@Wire
	Listbox lstSecondFields;
	@Wire
	Textbox txtCondition;
	@Wire
	Checkbox chbOuterJoin;
	@Wire
	Tab tabFirst;
	@Wire
	Tab tabSecond;
	
	Form form;
	
	HashMap<String, IField<?>> fields;

	public FormPropertiesDialog(Form form) throws Exception {
		super();
		
		this.form = form;
		
		/* create the ui */
		Executions.createComponents("/components/FormProperties.zul", this, null);
		Selectors.wireVariables(this, this, null);
		Selectors.wireComponents(this, this, false);
		Selectors.wireEventListeners(this, this);
		setBorder("normal");
		setWidth("50%");
		setHeight("50%");
		setClosable(false);
		setTitle("Form name");
		txtFormName.setValue(form.getTitle());
		
		addEventListeners();
		
		initUI();
	}
	
	private void initUI() throws Exception {
		FormFieldResolver resolver = new FormFieldResolver(form);
		fields = resolver.getFields();
		
		List<ResultColumn> resList = form.getResultList();
		if (resList == null) {
			form.setResultList(new ArrayList<ResultColumn>());
			resList = form.getResultList();
		}
		
		// clear listbox
		lstAvailableFields.getItems().clear();
		lstResultFields.getItems().clear();
		
		boolean contains = false;
		for(IField<?> f : fields.values()) {
			// it is not possible to use display only fields for the form result list
			if (f.isDisplayOnly())
				continue;
			
			Listitem item = new Listitem();
			ResultColumn column = resContains(resList, f.getId());
			if (column == null) {
				column = new ResultColumn(f.getId(), f.getLabel());
				contains = false;
			} else
				contains = true;
			
			item.setValue(column);
			Listcell cell = new Listcell();
			cell.setLabel(column.label + "(" + column.fieldName + ")");
			item.appendChild(cell);
			// if field in form result list
			if (contains) {
				lstResultFields.getItems().add(item);
			} else
				lstAvailableFields.getItems().add(item);
		}
		
		if (form.isJoin()) {
			tabJoin.setVisible(true);
			FormFieldResolver leftResolver = new FormFieldResolver(form.getLeftForm());
			fillFieldList(lstFirstFields, leftResolver.getFields().values());
			tabFirst.setLabel(form.getLeftForm().getName() + " fields");
			
			FormFieldResolver rightResolver = new FormFieldResolver(form.getRightForm());
			fillFieldList(lstSecondFields, rightResolver.getFields().values());
			tabSecond.setLabel(form.getRightForm().getName() + " fields");
			
			txtCondition.setText(form.getJoinClause());
			chbOuterJoin.setChecked(form.isOuterJoin());
		}
	}
	
	private void fillFieldList(Listbox list, Collection<IField<?>> fields) {
		list.getItems().clear();
		for(IField<?> f: fields) {
			Listitem item = new Listitem();
			Listcell id = new Listcell();
			Listcell title = new Listcell();
			id.setLabel(f.getId());
			title.setLabel(f.getLabel());
			item.appendChild(id);
			item.appendChild(title);
			item.setValue(f);
			list.getItems().add(item);
		}
	}
	
	private ResultColumn resContains(List<ResultColumn> arr, String str) {
		for (ResultColumn s : arr) {
			if (s.fieldName.equals(str))
				return s;
		}
		return null;
	}
	
	private void addEventListeners() {
		btnOK.addEventListener(Events.ON_CLICK, new EventListener<MouseEvent>() {

			@Override
			public void onEvent(MouseEvent arg0) throws Exception {
				btnOK_onClick();
			}
			
		});
		
		btnCancel.addEventListener(Events.ON_CLICK, new EventListener<MouseEvent>() {

			@Override
			public void onEvent(MouseEvent arg0) throws Exception {
				btnCancel_onClick();
			}
			
		});
		
		btnAdd.addEventListener(Events.ON_CLICK, new EventListener<MouseEvent>() {

			@Override
			public void onEvent(MouseEvent arg0) throws Exception {
				btnAdd_onClick();
			}
			
		});
		
		btnRemove.addEventListener(Events.ON_CLICK, new EventListener<MouseEvent>() {

			@Override
			public void onEvent(MouseEvent arg0) throws Exception {
				btnRemove_onClick();
			}
			
		});
		
		btnUpdateColumnLabel.addEventListener(Events.ON_CLICK, new EventListener<MouseEvent>() {

			@Override
			public void onEvent(MouseEvent arg0) throws Exception {
				btnUpdateColumnLabel_onClick();
			}
			
		});
		
		lstResultFields.addEventListener(Events.ON_SELECT, new EventListener<SelectEvent<?,?>>() {

			@Override
			public void onEvent(SelectEvent<?, ?> arg0) throws Exception {
				lstResultFields_onSelect();
			}
			
		});
		
		lstFirstFields.addEventListener(Events.ON_DOUBLE_CLICK, new EventListener<MouseEvent>() {

			@Override
			public void onEvent(MouseEvent arg0) throws Exception {
				if (lstFirstFields.getSelectedItem() != null) {
					IField<?> f = lstFirstFields.getSelectedItem().getValue();
					txtCondition.setText(txtCondition.getText() + "'" + f.getId() + "'");
				}
			}
			
		});
		
		lstSecondFields.addEventListener(Events.ON_DOUBLE_CLICK, new EventListener<MouseEvent>() {

			@Override
			public void onEvent(MouseEvent arg0) throws Exception {
				if (lstSecondFields.getSelectedItem() != null) {
					IField<?> f = lstSecondFields.getSelectedItem().getValue();
					txtCondition.setText(txtCondition.getText() + "`" + f.getId() + "`");
				}
			}
			
		});
	}
	
	protected void btnRemove_onClick() {
		if (lstResultFields.getSelectedItem() != null) {
			Listitem item = lstResultFields.getSelectedItem();
			lstResultFields.getItems().remove(item);
			lstAvailableFields.getItems().add(item);
			txtColumnLabel.setText("");
		}
	}

	protected void btnAdd_onClick() {
		if (lstAvailableFields.getSelectedItem() != null) {
			Listitem item = lstAvailableFields.getSelectedItem();
			lstAvailableFields.getItems().remove(item);
			lstResultFields.getItems().add(item);
		}
	}

	protected void btnCancel_onClick() {
		Event closeEvent = new Event(Events.ON_CLOSE, this);
		Events.postEvent(closeEvent);
		detach();
	}
	
	protected void lstResultFields_onSelect() {
		if (lstResultFields.getSelectedItem() != null) {
			txtColumnLabel.setText(
					((ResultColumn)lstResultFields.getSelectedItem().getValue()).label);
		}
	}
	
	protected void btnUpdateColumnLabel_onClick() {
		if (lstResultFields.getSelectedItem() != null) {
			ResultColumn c = (ResultColumn)lstResultFields.getSelectedItem().getValue(); 
			c.label = txtColumnLabel.getText();
			((Listcell)lstResultFields.getSelectedItem().getFirstChild()).setLabel(c.label + 
					"(" + c.fieldName + ")");
		}
	}

	private void btnOK_onClick() {
		if (lstResultFields.getItemCount() == 0) {
			Messagebox.show("Result list can not be empty.");
			return;
		}
		if (form.isJoin()) {
			try {
				checkJoinCondition();
			} catch (Exception e) {
				Messagebox.show("Invalid join condition: " + e.getMessage(), 
						"Condition Validation Error", Messagebox.OK, Messagebox.ERROR);
				return;
			}
		}
		ArrayList<ResultColumn> resLst = new ArrayList<ResultColumn>();
		for (int i=0; i<lstResultFields.getItemCount(); i++) {
			resLst.add((ResultColumn) lstResultFields.getItems().get(i).getValue());
		}
		form.setResultList(resLst);
		form.setTitle(txtFormName.getValue());
		
		if (form.isJoin()) {
			//TODO Check join condition;
			form.setOuterJoin(chbOuterJoin.isChecked());
			form.setJoinClause(txtCondition.getText());
		}
		
		// Close dialog
		Event closeEvent = new Event(Events.ON_CLOSE, this);
		Events.postEvent(closeEvent);
		detach();
	}
	
	private void checkJoinCondition() throws Exception {
		DatabaseConditionBuilder builder = new DatabaseConditionBuilder();
		
		FormFieldResolver resolver = new FormFieldResolver(form);
		
		builder.buildCondition(txtCondition.getText(), null, resolver, new ArrayList<Value<?>>(), 
				"left", "right", new HashMap<DBField, String>(), new HashMap<DBField, String>(), true);
	}
}
