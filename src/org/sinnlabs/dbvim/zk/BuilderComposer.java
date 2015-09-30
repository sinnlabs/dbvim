/**
 * 
 */
package org.sinnlabs.dbvim.zk;

import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.sinnlabs.dbvim.config.ConfigLoader;
import org.sinnlabs.dbvim.db.model.DBField;
import org.sinnlabs.dbvim.db.model.DBModel;
import org.sinnlabs.dbvim.model.Form;
import org.sinnlabs.dbvim.model.ResultColumn;
import org.sinnlabs.dbvim.rules.engine.Rules;
import org.sinnlabs.dbvim.rules.engine.RulesEngine;
import org.sinnlabs.dbvim.ui.Designer;
import org.sinnlabs.dbvim.ui.DesignerCanvas;
import org.sinnlabs.dbvim.ui.DesignerElements;
import org.sinnlabs.dbvim.ui.DesignerEvents;
import org.sinnlabs.dbvim.ui.DesignerProperties;
import org.sinnlabs.dbvim.ui.DesignerTree;
import org.sinnlabs.dbvim.ui.FormNameDialog;
import org.sinnlabs.dbvim.ui.FormPropertiesDialog;
import org.sinnlabs.dbvim.ui.ModelTree;
import org.sinnlabs.dbvim.ui.modeltree.TableTreeNode;
import org.sinnlabs.dbvim.zk.model.CanvasTreeSynchronizer;
import org.sinnlabs.dbvim.zk.model.ICurrentForm;
import org.sinnlabs.dbvim.zk.model.IDeveloperStudio;
import org.sinnlabs.dbvim.zk.model.ZUMLModel;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Messagebox;

/**
 * Composer represents Developer Studio
 * @author peter.liverovsky
 *
 */
public class BuilderComposer extends SelectorComposer<Component> implements
		IDeveloperStudio, ICurrentForm {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6375262218446359399L;

	private Form currentForm;

	/**
	 * The designer canvas
	 */
	@Wire
	DesignerCanvas designerCanvas;

	@Wire("#properties")
	DesignerProperties designerProperties;
	
	@Wire("#events")
	DesignerEvents designerEvents;

	@Wire("#wndDesigner")
	Designer designer;

	@Wire("#elements")
	DesignerElements elements;

	@Wire
	DesignerTree designerTree;

	@Wire("#modeltree")
	ModelTree modelTree;

	/**
	 * The Web application object
	 */
	protected WebApp webApp = null;

	/**
	 * The component's rules object
	 */
	protected Rules rules = null;

	/**
	 * The canvas tree synchronizer object
	 */
	protected CanvasTreeSynchronizer sync;

	/* Getters and Setters */
	@Override
	public DesignerTree getDesignerTree() {
		return designerTree;
	}

	@Override
	public Rules getRules() {
		return rules;
	}

	@Override
	public DesignerProperties getDesignerProperties() {
		return designerProperties;
	}
	
	@Override
	public DesignerEvents getDesignerEvents() {
		return designerEvents;
	}

	@Override
	public DesignerCanvas getDesignerCanvas() {
		return designerCanvas;
	}

	@Override
	public Designer getDesigner() {
		return designer;
	}

	@Override
	public CanvasTreeSynchronizer getSynchronizer() {
		return sync;
	}

	@Override
	public Form getCurrentForm() {
		return currentForm;
	}

	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);

		webApp = Executions.getCurrent().getDesktop().getWebApp();

		/* save current instance */
		/* DeveloperFactory will use this */
		Sessions.getCurrent().setAttribute("DEVELOPER", this);
		Sessions.getCurrent().setAttribute("CURRENTFORM", this);

		// create the model synchronizer
		sync = new CanvasTreeSynchronizer();

		loadRules();

		designerCanvas.setEditable(false);
	}

	/**
	 * Loads all predefined rules that apply to certain components from the
	 * 'rules.xml' configuration file.
	 */
	private void loadRules() {
		// get the real path of the configuration file
		String uri = webApp.getRealPath("/config/rules/rules.xml");

		if (StringUtils.isEmpty(uri))
			return;

		// load the rules using the rules engine
		rules = RulesEngine.loadComponentRules(uri);

	}

	private void checkStudioStates() {
		if (currentForm == null) {
			designerCanvas.setEditable(false);
		} else {
			designerCanvas.setEditable(true);
			designerCanvas.setTitle(currentForm.getName());
		}
	}

	@Listen("onClick = #tbbNewForm")
	public void tbbNewForm_onClick() {
		TableTreeNode table = modelTree.getSelectedTable();
		if (table == null) {
			Messagebox.show("Select a table first.", "Error", Messagebox.OK,
					Messagebox.EXCLAMATION);
			return;
		}
		if (currentForm != null) {
			Messagebox.show("Close form before creating the new one.");
		}
		if (currentForm == null) {
			currentForm = new Form();
			currentForm.setDBConnection(table.getConnection());
			currentForm.setName("New untitled form");
			currentForm.setTitle("Untitled Form");
			currentForm.setTableName(table.getTable().getName());
			currentForm.setCatalog(table.getTable().getCatalog());
			currentForm.setView("");
			currentForm.setJoin(false); // regular form
			setDefaultResultList(currentForm);
			checkStudioStates();
		}
	}
	
	@Listen("onClick = #tbbFormProperties")
	public void tbbFormProperties_onClick() {
		if (currentForm != null) {
			try {
				FormPropertiesDialog dialog = new FormPropertiesDialog(currentForm);
				designer.appendChild(dialog);
				dialog.doModal();
			} catch (ClassNotFoundException e) {
				Messagebox.show("Unable to open form properties: " + e.getMessage(), 
						"ERROR", Messagebox.OK, Messagebox.ERROR);
				System.err.println("ERROR: Unable to create form properties dialog.");
				e.printStackTrace();
			} catch (SQLException e) {
				Messagebox.show("Unable to open form properties: " + e.getMessage(), 
						"ERROR", Messagebox.OK, Messagebox.ERROR);
				System.err.println("ERROR: Unable to create form properties dialog.");
				e.printStackTrace();
			}
			designerCanvas.setDirty(true);
		}
	}

	private void setDefaultResultList(Form form) {
		try {
			DBModel model = new DBModel(form.getDBConnection()
					.getConnectionString(), form.getDBConnection().getClassName());
			
			List<DBField> fields = model.getFields(form.getCatalog(),
					form.getTableName());
			
			ArrayList<ResultColumn> res = new ArrayList<ResultColumn>();
			for (DBField f : fields) {
				if (f.isPrimaryKey()) {
					res.add(new ResultColumn(f.getName()));
				}
			}
			
			form.setResultList(res);
		} catch (SQLException e) {
			System.err.println("ERROR: Unable to get table field list: " + form);
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.err.println("ERROR: Unable to get table field list: " + form);
			e.printStackTrace();
		}
	}

	@Listen("onClick = #tbbSaveForm")
	public void tbbSaveForm_onClick() {
		if (currentForm != null) {
			formNameDialog();
		}
	}

	@Listen("onClick = #tbbCloseForm")
	public void tbbCloseForm_onClick() {
		if (currentForm == null)
			return;
		if (designerCanvas.isCanvasDirty()) {
			Messagebox.show("Do you want to save changes?", "Save changes", 
					Messagebox.YES | Messagebox.NO | Messagebox.CANCEL, 
					Messagebox.QUESTION, new EventListener<Event>() {

						@Override
						public void onEvent(Event evnt) throws Exception {
							if (Messagebox.ON_YES.equals(evnt.getName())) {
								saveForm();
								closeForm();
							} else if (Messagebox.ON_NO.equals(evnt.getName())) {
								closeForm();
							}
						}
			});
		} else {
			closeForm();
		}
	}
	
	private void closeForm() {
		currentForm = null;
		checkStudioStates();
	}
	
	private void formNameDialog() {
		final FormNameDialog dialog = new FormNameDialog(
				currentForm.getName());
		designer.appendChild(dialog);

		dialog.addEventListener(Events.ON_CLOSE,
				new EventListener<Event>() {

					@Override
					public void onEvent(Event evnt) throws Exception {
						currentForm.setName(dialog.getName());
						saveForm();
					}

				});

		dialog.doModal();
	}

	private void saveForm() {
		try {
			ZUMLModel model = designerCanvas.getZUMLRepresentation();
			currentForm.setView(model.getZUML());
			ConfigLoader.getInstance().getForms().createOrUpdate(currentForm);
			designerCanvas.setDirty(false);
		} catch (SQLException e) {
			System.err
					.println("Unable to save form into db: " + e.getMessage());
			e.printStackTrace();
			Messagebox.show("Unable to save form into db: " + e.getMessage(),
					"ERROR", Messagebox.OK, Messagebox.ERROR);
		}
	}

	@Override
	public void FormTreeNode_onDoubleClick(Form form) {
		if (currentForm == null) {
			loadForm(form);
		} else {
			Messagebox.show("Close current form first.");
		}
	}

	private void loadForm(Form form) {
		currentForm = form;
		StringReader reader = new StringReader(form.getView());
		designerCanvas.loadModelFromStream(reader, true);
		checkStudioStates();
	}

	@Override
	public Form getForm() {
		return currentForm;
	}

	@Override
	public DBField getDBFieldByMapping(String name) {
		try {
			DBModel model = new DBModel(currentForm.getDBConnection().getConnectionString(), 
					currentForm.getDBConnection().getClassName());
			DBField field = model.getField(currentForm.getCatalog(),
					currentForm.getTableName(), name);
			return field;
		} catch (ClassNotFoundException | SQLException e) {
			Messagebox.show("Unable to set mapping for db field name: " + name
					+ " db table: " + currentForm.getName());
			System.err.println("Unable to set mapping for db field name: "
					+ name + " db table: " + currentForm.getName());
			e.printStackTrace();
		}
		return null;
	}
}
