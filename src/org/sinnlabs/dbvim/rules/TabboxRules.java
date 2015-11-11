/**
 * 
 */
package org.sinnlabs.dbvim.rules;

import org.sinnlabs.dbvim.rules.engine.IRulable;
import org.sinnlabs.dbvim.rules.engine.RulesResult;
import org.sinnlabs.dbvim.rules.engine.exceptions.RulesException;
import org.sinnlabs.dbvim.zk.model.IDeveloperStudio;
import org.zkoss.zk.ui.Component;

/**
 * @author peter.liverovsky
 *
 */
public class TabboxRules implements IRulable {
	
	private String[] excludedProperties = new String[] {"zclass", "Zclass", "action", "autag", "widgetClass", 
			 "popup", "context", "style", "selectedIndex"};
	
	private String[] zumlExcludedProperties = new String[] {"style", "selectedIndex"};

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
		return zumlExcludedProperties;
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
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.rules.engine.IRulable#showChildren()
	 */
	@Override
	public boolean showChildren() {
		return true;
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
		return true;
	}

	/* (non-Javadoc)
	 * @see org.sinnlabs.dbvim.rules.engine.IRulable#getSpecialProperty(org.zkoss.zk.ui.Component, java.lang.String, org.sinnlabs.dbvim.zk.model.IDeveloperStudio)
	 */
	@Override
	public Component getSpecialProperty(Component cmp, String name,
			IDeveloperStudio developer) {
		// TODO Auto-generated method stub
		return null;
	}

}
