package org.dbvim.dbuibuilder.ui;

import java.io.Reader;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.dbvim.dbuibuilder.rules.engine.RulesEngine;
import org.dbvim.dbuibuilder.zk.model.ComponentFactory;
import org.dbvim.dbuibuilder.zk.model.DeveloperFactory;
import org.dbvim.dbuibuilder.zk.model.ElementInfo;
import org.dbvim.dbuibuilder.zk.model.IElementDesc;
import org.dbvim.dbuibuilder.zk.model.ZUMLModel;
import org.zkoss.idom.Document;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Components;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.HtmlBasedComponent;
import org.zkoss.zk.ui.event.DropEvent;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Absolutechildren;
import org.zkoss.zul.Absolutelayout;

public class DesignerCanvas extends DesignerWindow {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8656450007038538314L;

	public static final String DESIGNER_CANVAS_ID = "designerCanvas";

	/**
	 *  
	 */
	protected Component locatedComponent = null;

	/**
	 * Flag that indicates if the canvas model has differences compared to the
	 * last saved instance
	 */
	private boolean bIsCanvasDirty = false;

	/**
	 * Flag that indicates if the canvas is in editable mode
	 */
	private boolean bIsEditable = false;
	
	private Absolutelayout layout;

	// Getters / Setters
	public boolean isCanvasDirty() {
		return bIsCanvasDirty;
	}

	public boolean isEditable() {
		return bIsEditable;
	}

	/**
	 * 
	 * @param main
	 */
	public void onCreate() {
		// create the Canvas window
		setId(DESIGNER_CANVAS_ID);
		setTitle("New untitled form");
		setWidth("100%");
		setHeight("100%");
		setBorder("normal");
		setDroppable("false");
		
		layout = new Absolutelayout();
		layout.setVflex("1");
		layout.setHflex("1");
		layout.setDroppable("true");
		//this.appendChild(layout);
		final Absolutelayout al = layout;
		final DesignerCanvas tmp = this;
		layout.addEventListener(Events.ON_DROP, new EventListener<DropEvent>() {

			@Override
			public void onEvent(DropEvent evnt) throws Exception {
				if (evnt.getDragged() instanceof IElementDesc) {
					/*Messagebox.show(
							"Element dragged: "
									+ ((IElementDesc) evnt.getDragged())
											.getElementInfo().getClassName(),
							"Warning", Messagebox.OK, Messagebox.EXCLAMATION);*/
					ElementInfo info = ((IElementDesc) evnt.getDragged())
							.getElementInfo();
					HtmlBasedComponent comp = (HtmlBasedComponent) ComponentFactory
							.createComponent(info.getClassName());

					/*comp.setDroppable("true");
					comp.addEventListener(Events.ON_DROP,
							new EventListener<Event>() {
								public void onEvent(Event event)
										throws Exception {
									onEvent((DropEvent) event);
								}
							}); */
					Absolutechildren child = new Absolutechildren();
					child.setX(evnt.getX());
					child.setY(evnt.getY());
					child.appendChild(comp);
					child.setDraggable("true");
					al.appendChild(child);
					RulesEngine.applyRules(comp, RulesEngine.CREATION_RULES);
				} else if (evnt.getDragged() instanceof Absolutechildren) {
					Absolutechildren ac = (Absolutechildren) evnt.getDragged();
					ac.setX(evnt.getX());
					ac.setY(evnt.getY());
				}
				DeveloperFactory.getInstance().getSynchronizer()
						.synchronizeTreeWithCanvas(tmp);
				setDirty(true);
			}

		});
	}

	/**
	 * Returns the top most element in the drawing canvas.
	 */
	public Component getRootComponent() {
		// return the first child component of the canvas
		if ((getChildren() != null) && (getChildren().size() > 0))
			return (Component) getChildren().get(0);
		else
			return null;
	}

	/**
	 * Get component by UUID
	 * 
	 * @param sId component UUID
	 * @return component reference
	 */
	public Component getCanvasComponent(String sId) {
		if (StringUtils.isEmpty(sId))
			return null;

		locatedComponent = null;

		locateComponent(this.getRoot(), sId);

		return locatedComponent;
	}

	protected Component locateComponent(Component canvasComponent, String sId) {
		if (locatedComponent != null)
			return locatedComponent;

		if (canvasComponent == null)
			return null;

		// get the component's children
		List<Component> listChildren = canvasComponent.getChildren();

		if ((listChildren == null) || (listChildren.size() == 0))
			return null;

		// loop through all the component's children
		Iterator<Component> iter = listChildren.iterator();
		while (iter.hasNext()) {
			// get the next component in the list
			Component child = (Component) iter.next();

			if (child == null)
				continue;

			if (child.getUuid().equals(sId)) {
				locatedComponent = child;
				break;
			}

			// parse the model of the child
			locateComponent(child, sId);
		}

		return null;
	}

	/**
	 * Loads a page model from a file stream
	 * 
	 * @param modelReader
	 *            The stream Reader
	 * @param bSynchronizeTree
	 *            if <b>true</b> the tree is synchronized with the current
	 *            canvas model
	 */
	public void loadModelFromStream(Reader modelReader, boolean bSynchronizeTree) {
		if (modelReader == null)
			return;

		try {
			// remove all components from the canvas window
			Components.removeAllChildren(this);

			// create the component model described in the '*.zul' file
			// onto the designer Canvas
			Executions.createComponentsDirectly(modelReader,
					null, this, null);

			// synchronize the Tree with the model
			if (bSynchronizeTree)
				DeveloperFactory.getInstance().getSynchronizer()
						.synchronizeTreeWithCanvas(this);

			// turn the 'DIRTY' flag off
			bIsCanvasDirty = false;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads a page model from an iDOM Document
	 * 
	 * @param modelReader
	 *            The Document object that holds the model representation
	 * @param bSynchronizeTree
	 *            if <b>true</b> the tree is synchronized with the current
	 *            canvas model
	 */
	public void loadModelFromDocument(Document modelDocument,
			boolean bSynchronizeTree) {
		if (modelDocument == null)
			return;

		try {
			// remove all components from the canvas window
			Components.removeAllChildren(this);

			// create the component model described in the '*.zul' file
			// onto the designer Canvas
			// Executions.createComponentsDirectly(modelDocument, null, this,
			// null);
			Executions
					.createComponentsDirectly(modelDocument, null, this, null);

			// synchronize the Tree with the model
			if (bSynchronizeTree)
				DeveloperFactory.getInstance().getSynchronizer()
						.synchronizeTreeWithCanvas(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Converts the model into its ZUML equivalent using a custom convertor.
	 * 
	 * @return The convertor object that holds the ZUML model representation
	 */
	public ZUMLModel getZUMLRepresentation() {
		//if (getChildren().get(0) == null)
		//	return null;

		// create a model-to-ZUML convertor instance
		ZUMLModel model = new ZUMLModel(this);

		return model;
	}

	/**
	 * Deletes the current component model from the canvas.
	 * 
	 * @param evt
	 */
	public void clearCanvas() {
		try {
			// detach all the components from the canvas
			Components.removeAllChildren(this);

			// clear the Tree
			DeveloperFactory.getInstance().getDesignerTree().clearTree();

			// turn the 'DIRTY' flag on
			bIsCanvasDirty = true;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Refreshes the canvas by reloading the active model and synchronizing the
	 * tree.
	 */
	public void refreshCanvas() {
		// convert the current model into a ZUML Document
		// create a model-to-ZUML convertor instance
		try {
			ZUMLModel model = new ZUMLModel(this);
			// reload the model onto the canvas
			loadModelFromDocument(model.getZUMLDocument(), true);

		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	public void setEditable(boolean edit) {
		bIsEditable = edit;
		UpdateUI();
	}
	
	public void setDirty(boolean val) {
		bIsCanvasDirty = val;
		UpdateUI();
	}

	private void UpdateUI() {
		if (!bIsEditable) {
			setVisible(false);
			setDroppable("false");
			clearCanvas();
		}
		else {
			setVisible(true);
			setDroppable("false");
		}
		if (bIsCanvasDirty) {
			if (!getTitle().endsWith("*"))
				setTitle(getTitle() + "*");
		}
		else {
			if (getTitle().endsWith("*")) {
				setTitle(getTitle().substring(0, getTitle().length()-2));
			}
		}
	}
}
