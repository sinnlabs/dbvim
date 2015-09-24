package org.sinnlabs.dbvim.rules;

import java.sql.SQLException;

import org.sinnlabs.dbvim.model.Form;
import org.sinnlabs.dbvim.rules.Default.DefaultRules;
import org.sinnlabs.dbvim.rules.engine.IRulable;
import org.sinnlabs.dbvim.rules.engine.RulesResult;
import org.sinnlabs.dbvim.rules.engine.exceptions.RulesException;
import org.sinnlabs.dbvim.ui.IField;
import org.sinnlabs.dbvim.ui.SelectFieldDialog;
import org.sinnlabs.dbvim.zk.model.DeveloperFactory;
import org.sinnlabs.dbvim.zk.model.IDeveloperStudio;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

public class BaseFieldRules implements IRulable {
	
	protected int fType;
	
	protected BaseFieldRules(int fType) {
		this.fType = fType;
	}

	@Override
	public RulesResult applyPreCreationRules() throws RulesException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RulesResult applyCreationRules(Component cmp) throws RulesException {
		IDeveloperStudio dev = DeveloperFactory.getInstance();

		final IField<?> field = (IField<?>) cmp;

		Form current = dev.getCurrentForm();
		try {
			final SelectFieldDialog dialog = new SelectFieldDialog(current,
					fType);

			dialog.addEventListener(Events.ON_CLOSE,
					new EventListener<Event>() {

						@Override
						public void onEvent(Event arg0) throws Exception {
							if (dialog.getSelectedAction() == SelectFieldDialog.DD_OK) {
								field.setDBField(dialog.getSelectedField());
							} else {
								throw new RulesException(new RulesResult(
										RulesResult.ERR_UNSPECIFIED,
										"no field selected."));
							}
						}

					});
			dev.getDesigner().appendChild(dialog);

			dialog.doModal();

		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean exportChildrenToZUML() {
		// TODO Auto-generated method stub
		return false;
	}

}
