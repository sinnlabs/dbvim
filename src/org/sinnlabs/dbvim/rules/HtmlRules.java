/**
 * 
 */
package org.sinnlabs.dbvim.rules;

import org.sinnlabs.dbvim.rules.Default.DefaultRules;
import org.sinnlabs.dbvim.rules.engine.IRulable;
import org.sinnlabs.dbvim.rules.engine.RulesResult;
import org.sinnlabs.dbvim.rules.engine.exceptions.RulesException;
import org.sinnlabs.dbvim.ui.ExpandWindow;
import org.sinnlabs.dbvim.zk.model.IDeveloperStudio;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Html;
import org.zkoss.zul.Textbox;

/**
 * Class represents Html component rules
 * @author peter.liverovsky
 *
 */
public class HtmlRules implements IRulable {

	String[] excludedProperties = new String[] {"zclass", "action", "autag", "widgetClass", 
			 "droppable", "popup", "context", "draggable", "style"};
	String[] specialProperties = new String[] {"content"};
	
	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.rules.engine.IRulable#applyPreCreationRules(org.sinnlabs.dbvim.zk.model.IDeveloperStudio)
	 */
	@Override
	public RulesResult applyPreCreationRules(IDeveloperStudio developer)
			throws RulesException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.rules.engine.IRulable#applyCreationRules(org.zkoss.zk.ui.Component, org.sinnlabs.dbvim.zk.model.IDeveloperStudio)
	 */
	@Override
	public RulesResult applyCreationRules(Component cmp,
			IDeveloperStudio developer) throws RulesException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.rules.engine.IRulable#applyModelToZUMLRules(org.zkoss.zk.ui.Component)
	 */
	@Override
	public RulesResult applyModelToZUMLRules(Component cmp)
			throws RulesException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.rules.engine.IRulable#applyComponentDisplayRules(org.zkoss.zk.ui.Component)
	 */
	@Override
	public RulesResult applyComponentDisplayRules(Component cmp)
			throws RulesException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.rules.engine.IRulable#applyCopyRules(org.zkoss.zk.ui.Component)
	 */
	@Override
	public RulesResult applyCopyRules(Component source) throws RulesException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.rules.engine.IRulable#applyPrePasteRules(org.zkoss.zk.ui.Component, org.zkoss.zk.ui.Component)
	 */
	@Override
	public RulesResult applyPrePasteRules(Component clone, Component target)
			throws RulesException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.rules.engine.IRulable#getModelToZUMLExcludedAttributes()
	 */
	@Override
	public String[] getModelToZUMLExcludedAttributes() {
		return new DefaultRules().getModelToZUMLExcludedAttributes();
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.rules.engine.IRulable#getExcludedProperties()
	 */
	@Override
	public String[] getExcludedProperties() {
		return excludedProperties;
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.rules.engine.IRulable#getSpecialProperties()
	 */
	@Override
	public String[] getSpecialProperties() {
		return specialProperties;
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.rules.engine.IRulable#showChildren()
	 */
	@Override
	public boolean showChildren() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.rules.engine.IRulable#exportChildrenToZUML()
	 */
	@Override
	public boolean exportChildrenToZUML() {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.rules.engine.IRulable#exportChildToZUML(org.zkoss.zk.ui.Component)
	 */
	@Override
	public boolean exportChildToZUML(Component child) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.rules.engine.IRulable#getSpecialProperty(org.zkoss.zk.ui.Component, java.lang.String, org.sinnlabs.dbvim.zk.model.IDeveloperStudio)
	 */
	@Override
	public Component getSpecialProperty(Component cmp, String name,
			IDeveloperStudio developer) {
		if (name.equals("content")) {
			final IDeveloperStudio dev = developer;
			final Html html = (Html) cmp;
			
			// Create content property UI
			final Hlayout layout = new Hlayout();
			final Textbox txtContent = new Textbox();
			final Button btnExtend = new Button();
			txtContent.setMaxlength(0);
			txtContent.setRawValue(html.getContent());
			txtContent.setHflex("1");
			txtContent.setMultiline(true);
			txtContent.setRows(3);
			btnExtend.setLabel("...");
			layout.appendChild(txtContent);
			layout.appendChild(btnExtend);
			layout.setVflex("1");
			
			// Create event listeners
			EventListener<Event> txtEvent = new EventListener<Event>() {

				private Html cmp = html;
				
				@Override
				public void onEvent(Event arg0) throws Exception {
					cmp.setContent(txtContent.getText());
				}
				
			};
			txtContent.addEventListener(Events.ON_CHANGE, txtEvent);
			txtContent.addEventListener(Events.ON_OK, txtEvent);
			
			EventListener<Event> btnClick = new EventListener<Event>() {

				@Override
				public void onEvent(Event arg0) throws Exception {
					ExpandWindow expandBox = new ExpandWindow();
					CloseExpandBoxEventListener listener = new CloseExpandBoxEventListener(html, txtContent);
					expandBox.setTitle("Content");
					expandBox.setText(txtContent.getText());
					expandBox.addEventListener(Events.ON_CLOSE, listener);
					expandBox.setPosition("center");
					dev.getDesigner().appendChild(expandBox);
					expandBox.doOverlapped();
				}
				
			};
			btnExtend.addEventListener(Events.ON_CLICK, btnClick);
			
			return layout;
		}
		return null;
	}
	
	private class CloseExpandBoxEventListener implements EventListener<Event> {
		
		private Html html;
		private Textbox txt;
		
		public CloseExpandBoxEventListener(Html html, Textbox txt) {
			this.html = html;
			this.txt = txt;
		}

		/* (non-Javadoc)
		 * @see org.zkoss.zk.ui.event.EventListener#onEvent(org.zkoss.zk.ui.event.Event)
		 */
		@Override
		public void onEvent(Event arg0) throws Exception {
			ExpandWindow expandBox = (ExpandWindow) arg0.getTarget();
			if (expandBox.getSelectedAction() == ExpandWindow.DD_OK) {
				html.setContent(expandBox.getText());
				txt.setText(expandBox.getText());
			}
		}
		
	}
}
