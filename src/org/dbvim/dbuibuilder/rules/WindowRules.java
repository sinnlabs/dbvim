package org.dbvim.dbuibuilder.rules;

import org.dbvim.dbuibuilder.rules.Default.DefaultRules;
import org.dbvim.dbuibuilder.rules.engine.IRulable;
import org.dbvim.dbuibuilder.rules.engine.RulesResult;
import org.dbvim.dbuibuilder.rules.engine.exceptions.RulesException;
import org.zkoss.zk.ui.Component;
import org.zkoss.zul.Window;

public class WindowRules implements IRulable {
	
	private static final String title = "Untitled";

	@Override
	public RulesResult applyPreCreationRules() throws RulesException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RulesResult applyCreationRules(Component cmp) throws RulesException {
		if( cmp == null )
			return null;
		
		((Window)cmp).setTitle(title);
		((Window)cmp).setBorder("normal");
		
		return new RulesResult(RulesResult.SUCCESS, "");
	}

	@Override
	public RulesResult applyModelToZUMLRules(Component cmp)
			throws RulesException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RulesResult applyComponentDisplayRules(Component cmp)
			throws RulesException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RulesResult applyCopyRules(Component source) throws RulesException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RulesResult applyPrePasteRules(Component clone, Component target)
			throws RulesException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getModelToZUMLExcludedAttributes() {
		return new DefaultRules().getModelToZUMLExcludedAttributes();
	}

	@Override
	public String[] getExcludedProperties() {
		return new DefaultRules().getModelToZUMLExcludedAttributes();
	}

	@Override
	public boolean showChildren() {
		return true;
	}

	@Override
	public boolean exportChildrenToZUML() {
		return true;
	}

}
