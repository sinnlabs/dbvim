/**
 * 
 */
package org.sinnlabs.dbvim.rules;

import org.sinnlabs.dbvim.rules.Default.DefaultRules;
import org.sinnlabs.dbvim.rules.engine.IRulable;
import org.sinnlabs.dbvim.rules.engine.RulesResult;
import org.sinnlabs.dbvim.rules.engine.exceptions.RulesException;
import org.sinnlabs.dbvim.ui.TableFieldProperties;
import org.sinnlabs.dbvim.ui.db.TableColumnField;
import org.sinnlabs.dbvim.ui.db.TableField;
import org.sinnlabs.dbvim.zk.model.IDeveloperStudio;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
import org.zkoss.zul.Button;

/**
 * Class represents TableField rules
 * @author peter.liverovsky
 *
 */
public class TableFieldRules implements IRulable {

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.rules.engine.IRulable#applyPreCreationRules()
	 */
	@Override
	public RulesResult applyPreCreationRules(IDeveloperStudio developer) throws RulesException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.rules.engine.IRulable#applyCreationRules(org.zkoss.zk.ui.Component)
	 */
	@Override
	public RulesResult applyCreationRules(Component cmp, IDeveloperStudio developer) throws RulesException {
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
		return new DefaultRules().getExcludedProperties();
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.rules.engine.IRulable#showChildren()
	 */
	@Override
	public boolean showChildren() {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.rules.engine.IRulable#exportChildrenToZUML()
	 */
	@Override
	public boolean exportChildrenToZUML() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.rules.engine.IRulable#exportChildToZUML(org.zkoss.zk.ui.Component)
	 */
	@Override
	public boolean exportChildToZUML(Component child) {
		if (!(child instanceof TableColumnField))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.rules.engine.IRulable#getSpecialProperties()
	 */
	@Override
	public String[] getSpecialProperties() {
		String[] props = new String[] { "tableColumns" };
		return props;
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.rules.engine.IRulable#getSpecialProperty(java.lang.String)
	 */
	@Override
	public Component getSpecialProperty(final Component cmp, String name, IDeveloperStudio dev) {
		Button edit = new Button("Edit");
		final IDeveloperStudio developer = dev;
		
		edit.addEventListener(Events.ON_CLICK, new EventListener<MouseEvent>() {

			@Override
			public void onEvent(MouseEvent arg0) throws Exception {
				TableFieldProperties dialog = new TableFieldProperties((TableField)cmp);
				developer.getDesigner().appendChild(dialog);
				dialog.doModal();
			}
			
		});
		
		return edit;
	}

}
