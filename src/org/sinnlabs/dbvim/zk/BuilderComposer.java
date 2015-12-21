/**
 * 
 */
package org.sinnlabs.dbvim.zk;

import java.io.StringReader;
import java.sql.SQLException;
import java.util.ArrayList;

import org.sinnlabs.dbvim.config.ConfigLoader;
import org.sinnlabs.dbvim.form.FormFieldResolverFactory;
import org.sinnlabs.dbvim.model.CharacterMenu;
import org.sinnlabs.dbvim.model.Form;
import org.sinnlabs.dbvim.model.ResultColumn;
import org.sinnlabs.dbvim.model.SearchMenu;
import org.sinnlabs.dbvim.ui.CharacterMenuProperties;
import org.sinnlabs.dbvim.ui.CreateJoinFormDialog;
import org.sinnlabs.dbvim.ui.Designer;
import org.sinnlabs.dbvim.ui.DesignerCanvas;
import org.sinnlabs.dbvim.ui.DesignerElements;
import org.sinnlabs.dbvim.ui.DesignerEvents;
import org.sinnlabs.dbvim.ui.DesignerProperties;
import org.sinnlabs.dbvim.ui.DesignerTree;
import org.sinnlabs.dbvim.ui.FormNameDialog;
import org.sinnlabs.dbvim.ui.FormPropertiesDialog;
import org.sinnlabs.dbvim.ui.IField;
import org.sinnlabs.dbvim.ui.ModelTree;
import org.sinnlabs.dbvim.ui.SearchMenuProperties;
import org.sinnlabs.dbvim.ui.events.ComponentDeletedEvent;
import org.sinnlabs.dbvim.ui.modeltree.TableTreeNode;
import org.sinnlabs.dbvim.zk.model.CanvasTreeSynchronizer;
import org.sinnlabs.dbvim.zk.model.IDeveloperStudio;
import org.sinnlabs.dbvim.zk.model.ZUMLModel;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.metainfo.ComponentInfo;
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
		IDeveloperStudio {

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
	 * The canvas tree synchronizer object
	 */
	protected CanvasTreeSynchronizer sync;

	/* Getters and Setters */
	@Override
	public DesignerTree getDesignerTree() {
		return designerTree;
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
	
	public ComponentInfo doBeforeCompose(Page page,
            Component parent,
            ComponentInfo compInfo) {
		Executions.getCurrent().setAttribute("composer", this);
		return compInfo;
	}

	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);

		webApp = Executions.getCurrent().getDesktop().getWebApp();

		/* save current instance */
		/* DeveloperFactory will use this */
		Executions.getCurrent().setAttribute("composer", this);
		
		// create the model synchronizer
		sync = new CanvasTreeSynchronizer(this);

		designerCanvas.setEditable(false);
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
	
	@Listen("onClick = #tbbNewJoinForm")
	public void tbbNewJoinForm_onClick() {
		if (currentForm != null) {
			Messagebox.show("Close form before creating the new one.");
		}
		if (currentForm == null) {
			final Form form = new Form();
			form.setName("New untitled form");
			form.setTitle("Untitled Form");
			form.setView("");
			form.setJoin(true); //join regular form
			try {
				final CreateJoinFormDialog dialog = new CreateJoinFormDialog(form);
				dialog.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

					@Override
					public void onEvent(Event arg0) throws Exception {
						if (dialog.getSelectedAction() == CreateJoinFormDialog.DD_OK) {
							currentForm = form;
							checkStudioStates();
						}
					}
					
				});
				designer.appendChild(dialog);
				dialog.setWidth("50%");
				dialog.setHeight("70%");
				dialog.doModal();
			} catch (Exception e) {
				Messagebox.show("Unable to create join form dialog. " + e.getMessage());
				e.printStackTrace();
			}
			//setDefaultResultList(currentForm);
			checkStudioStates();
		}
	}
	
	@Listen("onClick = #tbbNewSearchMenu")
	public void tbbNewSearchMenu_onClick() {
		final SearchMenu menu = new SearchMenu();
		final SearchMenuProperties dialog = new SearchMenuProperties(menu, true);
		dialog.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

			@Override
			public void onEvent(Event arg0) throws Exception {
				if (dialog.getSelectedAction() == SearchMenuProperties.DD_OK) {
					ConfigLoader.getInstance().getSearchMenus().create(menu);
				}
			}
			
		});
		designer.appendChild(dialog);
		dialog.doModal();
	}
	
	@Listen("onClick = #tbbNewCharacterMenu")
	public void tbbNewCharacterMenu_onClick() {
		CharacterMenuProperties dialog = new CharacterMenuProperties(null);
		designer.appendChild(dialog);
		
		dialog.doModal();
	}
	
	@Listen("onClick = #tbbFormProperties")
	public void tbbFormProperties_onClick() {
		if (currentForm != null) {
			try {
				// update current form view definition
				updateViewDefinition();
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
			} catch (Exception e) {
				Messagebox.show("Unable to open form properties: " + e.getMessage(), 
						"ERROR", Messagebox.OK, Messagebox.ERROR);
				System.err.println("ERROR: Unable to create form properties dialog.");
				e.printStackTrace();
			}
			designerCanvas.setDirty(true);
		}
	}
	
	@Listen("onComponentDeleted = #designerTree")
	public void designerTree_onComponentDeleted(ComponentDeletedEvent evnt) {
		if (evnt.getDeletedComponent() != null && evnt.getDeletedComponent() instanceof IField<?>) {
			// rebuild form ResultList
			if (currentForm.getResultList() == null)
				return;
			for (ResultColumn c : currentForm.getResultList()) {
				if (c.fieldName.equals(evnt.getDeletedComponent().getId())) {
					currentForm.getResultList().remove(c);
					break;
				}
			}
		}
	}

	private void setDefaultResultList(Form form) {
		//try {
			//DBModel model = new DBModel(form.getDBConnection()
			//		.getConnectionString(), form.getDBConnection().getClassName());
			
			//List<DBField> fields = model.getFields(form.getCatalog(),
			//		form.getTableName());
			
			ArrayList<ResultColumn> res = new ArrayList<ResultColumn>();
			//for (DBField f : fields) {
			//	if (f.isPrimaryKey()) {
			//		res.add(new ResultColumn(f.getName()));
			//	}
			//}
			
			form.setResultList(res);
		/*} catch (SQLException e) {
			System.err.println("ERROR: Unable to get table field list: " + form);
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.err.println("ERROR: Unable to get table field list: " + form);
			e.printStackTrace();
		}*/
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
			updateViewDefinition();
			ConfigLoader.getInstance().getForms().createOrUpdate(currentForm);
			designerCanvas.setDirty(false);
			FormFieldResolverFactory.refreshItem(currentForm);
		} catch (SQLException e) {
			System.err
					.println("Unable to save form into db: " + e.getMessage());
			e.printStackTrace();
			Messagebox.show("Unable to save form into db: " + e.getMessage(),
					"ERROR", Messagebox.OK, Messagebox.ERROR);
		} catch (Exception e) {
			System.err.println("Unable to update cache for form: " + currentForm.getName() + " " + 
					e.getMessage());
			e.printStackTrace();
			Messagebox.show("Unable to update cache for form: " + currentForm.getName() + " " + 
					e.getMessage());
		}
	}
	
	private void updateViewDefinition() {
		ZUMLModel model = designerCanvas.getZUMLRepresentation();
		currentForm.setView(model.getZUML());
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

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.zk.model.IDeveloperStudio#MenuTreeNode_onDoubleClick(org.sinnlabs.dbvim.model.SearchMenu)
	 */
	@Override
	public void MenuTreeNode_onDoubleClick(final Object menu) {
		if (menu instanceof SearchMenu) {
			final SearchMenuProperties dialog = new SearchMenuProperties((SearchMenu) menu, false);
			dialog.addEventListener(Events.ON_CLOSE, new EventListener<Event>() {

				@Override
				public void onEvent(Event arg0) throws Exception {
					if (dialog.getSelectedAction() == SearchMenuProperties.DD_OK) {
						ConfigLoader.getInstance().getSearchMenus().update((SearchMenu) menu);
					}
				}

			});
			designer.appendChild(dialog);
			dialog.doModal();
		}
		if (menu instanceof CharacterMenu) {
			CharacterMenuProperties dialog = new CharacterMenuProperties((CharacterMenu) menu);
			designer.appendChild(dialog);
			dialog.doModal();
		}
	}
}
