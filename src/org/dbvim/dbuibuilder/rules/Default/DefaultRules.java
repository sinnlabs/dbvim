/**
 * 
 */
package org.dbvim.dbuibuilder.rules.Default;

import org.dbvim.dbuibuilder.rules.engine.IRulable;
import org.dbvim.dbuibuilder.rules.engine.RulesResult;
import org.dbvim.dbuibuilder.rules.engine.exceptions.RulesException;
import org.zkoss.zk.ui.Component;

/**
 * 
 * @author peter.liverovsky
 *
 */
public class DefaultRules implements IRulable {

	/* (non-Javadoc)
	 * @see org.dbvim.dbuibuilder.rules.engine.IRulable#applyPreCreationRules()
	 */
	@Override
	public RulesResult applyPreCreationRules() throws RulesException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.dbvim.dbuibuilder.rules.engine.IRulable#applyCreationRules(org.zkoss.zk.ui.Component)
	 */
	@Override
	public RulesResult applyCreationRules(Component cmp) throws RulesException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.dbvim.dbuibuilder.rules.engine.IRulable#applyModelToZUMLRules(org.zkoss.zk.ui.Component)
	 */
	@Override
	public RulesResult applyModelToZUMLRules(Component cmp)
			throws RulesException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.dbvim.dbuibuilder.rules.engine.IRulable#applyComponentDisplayRules(org.zkoss.zk.ui.Component)
	 */
	@Override
	public RulesResult applyComponentDisplayRules(Component cmp)
			throws RulesException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.dbvim.dbuibuilder.rules.engine.IRulable#applyCopyRules(org.zkoss.zk.ui.Component)
	 */
	@Override
	public RulesResult applyCopyRules(Component source) throws RulesException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.dbvim.dbuibuilder.rules.engine.IRulable#applyPrePasteRules(org.zkoss.zk.ui.Component, org.zkoss.zk.ui.Component)
	 */
	@Override
	public RulesResult applyPrePasteRules(Component clone, Component target)
			throws RulesException {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.dbvim.dbuibuilder.rules.engine.IRulable#getModelToZUMLExcludedAttributes()
	 */
	@Override
	public String[] getModelToZUMLExcludedAttributes() {
		String[] excluded = {"style"};
		return excluded;
	}

	/* (non-Javadoc)
	 * @see org.dbvim.dbuibuilder.rules.engine.IRulable#getExcludedProperties()
	 */
	@Override
	public String[] getExcludedProperties() {
		String[] excluded = {"style"};
		return excluded;
	}

	/* (non-Javadoc)
	 * @see org.dbvim.dbuibuilder.rules.engine.IRulable#showChildren()
	 */
	@Override
	public boolean showChildren() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.dbvim.dbuibuilder.rules.engine.IRulable#exportChildrenToZUML()
	 */
	@Override
	public boolean exportChildrenToZUML() {
		return true;
	}
}
