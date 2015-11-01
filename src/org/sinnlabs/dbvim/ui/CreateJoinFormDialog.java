/**
 * 
 */
package org.sinnlabs.dbvim.ui;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

import org.sinnlabs.dbvim.config.ConfigLoader;
import org.sinnlabs.dbvim.form.FormFieldResolver;
import org.sinnlabs.dbvim.form.FormFieldResolverFactory;
import org.sinnlabs.dbvim.model.DBConnection;
import org.sinnlabs.dbvim.model.Form;
import org.zkoss.zk.ui.Component;
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
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.stmt.Where;

/**
 * Class represents create new join form wizard
 * @author peter.liverovsky
 *
 */
public class CreateJoinFormDialog extends Window {

	/**
	 * 
	 */
	private static final long serialVersionUID = 400257598620048852L;
	
	/**
	 * Cancel dialog action
	 */
	public static final int DD_CANCEL = 0;

	/**
	 * Create new connection
	 */
	public static final int DD_OK = 1;

	/**
	 * The selected action
	 */
	private int nSelectedAction = DD_CANCEL;

	/**
	 * Current form
	 */
	private Form form;
	
	private Form leftForm = null;
	
	private Form rightForm = null;
	
	private DBConnection dbConnection = null;
	
	@Wire
	protected Button btnCancel;
	@Wire
	protected Button btnNext;
	@Wire("#stage1")
	protected Hlayout stage1;
	@Wire("#stage2")
	protected Hlayout stage2;
	@Wire("#stage0")
	protected Hlayout stage0;
	@Wire
	protected Listbox lstFirstFields;
	@Wire
	protected Listbox lstSecondFields;
	@Wire
	protected Listbox lstFirst;
	@Wire
	protected Listbox lstSecond;
	@Wire
	protected Listbox lstConnection;
	@Wire
	protected Tab tabFirst;
	@Wire
	protected Tab tabSecond;
	@Wire
	protected Textbox txtCondition;
	@Wire
	protected Checkbox chbOuterJoin;
	
	private Component[] stages;
	private int stage = 0;
	
	/**
	 * Returns user selected action
	 * @return DD_OK if user clicked ok, otherwise DD_CANCEL
	 */
	public int getSelectedAction() { return nSelectedAction; }
	
	/**
	 * Creates new instance
	 * @param form - Join form
	 * @throws Exception 
	 */
	public CreateJoinFormDialog(Form form) throws Exception {
		super();
		if (!form.isJoin()) {
			throw new IllegalArgumentException("Form must be join.");
		}
		
		this.form = form;
		
		// create the ui
		Executions
			.createComponents("/components/createjoinformdialog.zul", this, null);
		Selectors.wireVariables(this, this, null);
		Selectors.wireComponents(this, this, false);
		Selectors.wireEventListeners(this, this);
		setBorder("normal");
		setClosable(true);
		setTitle("New join form wizard");
		
		addEventListeners();
		
		stages = new Component[3];
		stages[0] = stage0;
		stages[1] = stage1;
		stages[2] = stage2;
		
		showStage(0);
		initConnectionList();
	}

	/**
	 * Creates event listeners
	 */
	private void addEventListeners() {
		btnCancel.addEventListener(Events.ON_CLICK, new EventListener<MouseEvent>() {

			@Override
			public void onEvent(MouseEvent arg0) throws Exception {
				btnCancel_onClick();
			}
			
		});
		
		btnNext.addEventListener(Events.ON_CLICK, new EventListener<MouseEvent>() {

			@Override
			public void onEvent(MouseEvent arg0) throws Exception {
				btnNext_onClick();
			}
			
		});
		
		lstConnection.addEventListener(Events.ON_SELECT, new EventListener<SelectEvent<?, ?>>() {

			@Override
			public void onEvent(SelectEvent<?, ?> arg0) throws Exception {
				lstConnection_onSelect();
			}
			
		});
		
		lstFirst.addEventListener(Events.ON_SELECT, new EventListener<SelectEvent<?,?>>() {

			@Override
			public void onEvent(SelectEvent<?, ?> arg0) throws Exception {
				lstFirst_onSelect();
			}
			
		});
		
		lstSecond.addEventListener(Events.ON_SELECT, new EventListener<SelectEvent<?,?>>() {

			@Override
			public void onEvent(SelectEvent<?, ?> arg0) throws Exception {
				lstSecond_onSelect();
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
	
	private void btnNext_onClick() throws Exception {
		if (stage == 0) {
			if (dbConnection == null) {
				Messagebox.show("Select connection.", "Error", Messagebox.OK, Messagebox.EXCLAMATION);
				return;
			}
			showStage(1);
			return;
		}
		if (stage == 1) {
			if(leftForm == null || rightForm == null) {
				Messagebox.show("Select first and second forms.", "Error", Messagebox.OK, Messagebox.EXCLAMATION);
				return;
			}
			showStage(2);
			return;
		}
		if (stage == 2) {
			// TODO validate condition
			form.setDBConnection(dbConnection);
			form.setJoin(true);
			form.setLeftForm(leftForm);
			form.setRigthForm(rightForm);
			form.setJoinClause(txtCondition.getText());
			form.setOuterJoin(chbOuterJoin.isChecked());
			// close window
			nSelectedAction = DD_OK;
			Event closeEvent = new Event(Events.ON_CLOSE, this);
			Events.postEvent(closeEvent);
			detach();
		}
	}
	
	private void btnCancel_onClick() {
		nSelectedAction = DD_CANCEL;
		Event closeEvent = new Event(Events.ON_CLOSE, this);
		Events.postEvent(closeEvent);
		detach();
	}
	
	private void lstConnection_onSelect() {
		if (lstConnection.getSelectedItem() != null) {
			dbConnection = lstConnection.getSelectedItem().getValue();
		}
	}
	
	private void lstFirst_onSelect() {
		if (lstFirst.getSelectedItem() != null) {
			leftForm = lstFirst.getSelectedItem().getValue();
		}
	}
	
	private void lstSecond_onSelect() {
		if (lstSecond.getSelectedItem() != null) {
			rightForm = lstSecond.getSelectedItem().getValue();
		}
	}
	
	private void showStage(int stage) throws Exception {
		for (int i=0; i<stages.length; i++) {
			if (i!=stage)
				stages[i].setVisible(false);
			else
				stages[i].setVisible(true);
		}
		switch(stage) {
		case 0:
			break;
		case 1:
			initFormList();
			break;
		case 2:
			initStage2();
			btnNext.setLabel("Finish");
			break;
		}
		this.stage = stage;
	}
	
	private void initFormList() throws SQLException {
		initFormList(lstFirst);
		initFormList(lstSecond);
	}
	
	private void initFormList(Listbox list) throws SQLException {
		list.getItems().clear();
		QueryBuilder<Form, String> qb = ConfigLoader.getInstance().getForms().queryBuilder();
		Where<Form, String> w = qb.where();
		w.eq(Form.CONNECTION_FIELD_NAME, dbConnection.getName());
		List<Form> forms = ConfigLoader.getInstance().getForms().query(qb.prepare());
		for(Form f : forms) {
			Listitem item = new Listitem();
			Listcell name = new Listcell();
			name.setLabel(f.getName());
			item.appendChild(name);
			item.setValue(f);
			list.getItems().add(item);
		}
	}
	
	private void initConnectionList() throws SQLException {
		lstConnection.getItems().clear();
		for(DBConnection c : ConfigLoader.getInstance().getDBConnections().queryForAll()) {
			Listitem item = new Listitem();
			Listcell cell = new Listcell();
			cell.setLabel(c.getName());
			item.appendChild(cell);
			item.setValue(c);
			lstConnection.getItems().add(item);
		}
	}
	
	private void initStage2() throws Exception {
		if (leftForm == null)
			return;
		FormFieldResolver leftResolver = FormFieldResolverFactory.getResolver(leftForm);
		fillFieldList(lstFirstFields, leftResolver.getFields().values());
		tabFirst.setLabel(leftForm.getName() + " fields");
		
		if (rightForm == null)
			return;
		FormFieldResolver rightResolver = FormFieldResolverFactory.getResolver(rightForm);
		fillFieldList(lstSecondFields, rightResolver.getFields().values());
		tabSecond.setLabel(rightForm.getName() + " fields");
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
}
