package org.sinnlabs.dbvim.rules.engine;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.sinnlabs.dbvim.config.Configurator;
import org.sinnlabs.dbvim.rules.Default.DefaultRules;
import org.sinnlabs.dbvim.rules.engine.exceptions.RulesException;
import org.sinnlabs.dbvim.zk.model.DeveloperFactory;
import org.zkoss.idom.Element;
import org.zkoss.zk.ui.Component;

public class RulesEngine {
	/**
	 * Applies the 'Pre-Creation' rules of a component 
	 */
	public static final int PRE_CREATION_RULES = 1;

	/**
	 * Applies the 'Creation' rules of a component 
	 */
	public static final int CREATION_RULES = 2;

	/**
	 * Applies the 'Model-to-ZUML' rules of a component  
	 */
	public static final int MODEL_TO_ZUML_RULES = 3;

	/**
	 * Applies the 'Treeview Display' rules of a component  
	 */
	public static final int COMPONENT_TREEVIEW_DISPLAY = 4;

	/**
	 * Applies the 'Copy' rules of a component 
	 */
	public static final int COPY_RULES = 5;

	/**
	 * Applies the 'Pre-Paste' rules of a component
	 */
	public static final int PRE_PASTE_RULES = 6;

	/**
	 * Denotes component attributes that will not
	 * be displayed in the ZUML representation 
	 */
	public static final int ATTRIBUTES_EXCLUDE_FROM_ZUML = 1;

	/**
	 * Denotes component attributes that should not be displayed
	 * onto the property view dialog, so that they cannot be changed
	 * directly by the user.
	 */
	public static final int ATTRIBUTES_EXCLUDE_FROM_PROPERTY_VIEW = 2;

	/**
	 * Denotes a boolean flag that indicates whether a
	 * component's children should be displayed onto
	 * the model treeview.
	 */
	public static final int FLAG_SHOW_CHILDREN = 1;

	/**
	 * Denotes a boolean flag that indicates whether a
	 * component's children should be exported to the
	 * ZUML file.
	 */
	public static final int FLAG_EXPORT_CHILDREN_TO_ZUML = 2;

	/**
	 * Creates an instance of the 'IRulable' component
	 * that corresponds to the specified visual element, 
	 * by retrieving its class and appending the 'Rules'
	 * suffix. There must be a runtime class available
	 * that implements the 'IRulable' interface for this
	 * reason. <p><p> 
	 * For example, if there is a custom component called
	 * <b>'CustomComponent'</b>, there must be a class defined 
	 * called <b>'CustomComponentRules'</b> that implements 
	 * the IRulable interface in order for the rules to be applied. 
	 * @param cmp The component that the rules engine
	 * will operate upon 
	 * @param nRulesType The type of rules to be applied
	 */
	public static RulesResult applyRules(Component cmp,	
			int nRulesType) throws RulesException
	{
		// get the rules class of the given component
		IRulable rulable = getRulesClassInstance(cmp);

		if (rulable == null)
			return null;

		// decide on the rules that should be applied
		switch (nRulesType)
		{
		/*** Apply pre-creation rules ***/
		case PRE_CREATION_RULES:
			return rulable.applyPreCreationRules();

			/*** Apply creation rules ***/
		case CREATION_RULES:
			return rulable.applyCreationRules(cmp);

			/*** Model-to-ZUML rules ***/
		case MODEL_TO_ZUML_RULES:
			return rulable.applyModelToZUMLRules(cmp);

			/*** Treeview Display rules ***/
		case COMPONENT_TREEVIEW_DISPLAY:
			return rulable.applyComponentDisplayRules(cmp);

			/*** Copy rules ***/
		case COPY_RULES:
			return rulable.applyCreationRules(cmp);
		}

		return null;
	}

	/**
	 * Creates an instance of the 'IRulable' component
	 * that corresponds to the specified visual element, 
	 * by retrieving its class and appending the 'Rules'
	 * suffix. There must be a runtime class available
	 * that implements the 'IRulable' interface for this
	 * reason. <p><p> 
	 * For example, if there is a custom component called
	 * <b>'CustomComponent'</b>, there must be a class defined 
	 * called <b>'CustomComponentRules'</b> that implements 
	 * the IRulable interface in order for the rules to be applied. 
	 * @param cmp The component that the rules engine
	 * will operate upon 
	 * @param nRulesType The type of rules to be applied
	 */
	public static RulesResult applyRules(Component source,
			Component target,
			int nRulesType) throws RulesException
	{
		// get the rules class of the given component
		IRulable rulable = getRulesClassInstance(source);

		if (rulable == null)
			return null;

		// decide on the rules that should be applied
		switch (nRulesType)
		{
		/*** Apply pre-paste rules ***/
		case PRE_PASTE_RULES:
			return rulable.applyPrePasteRules(source, target);
		}

		return null;
	}

	/**
	 * Returns a specified set of component attributes, 
	 * as it is defined in the rules class.
	 * @param cmp The component to be resolved
	 * @param nAttributesType The type of attributes to 
	 * be fetched.
	 * @return A String[] holding the requested attributes
	 */
	public static String[] getComponentAttributes(Component cmp, 
			int nAttributesType)
	{
		// get the rules class of the given component
		IRulable rulable = getRulesClassInstance(cmp);

		if (rulable == null)
			return null;

		try
		{
			// decide on which type of attributes
			// should be fetched
			switch (nAttributesType)
			{
			/*** Attributes to be excluded from ZUML definition ***/
			case ATTRIBUTES_EXCLUDE_FROM_ZUML:
				return rulable.getModelToZUMLExcludedAttributes();

				/*** Attributes to be excluded from the Property View dialog ***/
			case ATTRIBUTES_EXCLUDE_FROM_PROPERTY_VIEW:
				return rulable.getExcludedProperties();		
			}

			return null;
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	/**
	 * Returns a boolean indicating whether the componen's 
	 * child should be exported or not to the ZUML 
	 * representation
	 * @param cmp The component to be resolved
	 * @param child Child component to export
	 * @return true if component should be exported otherwise false
	 */
	public static boolean exportChildToZuml(Component cmp, Component child) {
		// get the rules class of the given component
		IRulable rulable = getRulesClassInstance(cmp);

		if (rulable == null)
			return true;

		try
		{
			return rulable.exportChildToZUML(child);
		}
		catch (Exception e)
		{
		}

		return true;
	}

	/**
	 * Returns the requested flag's value
	 * @param cmp The component to be resolved
	 * @param nAttributesType The type of attributes to 
	 * be fetched.
	 * @return A boolean flag
	 */
	public static boolean getComponentFlag(Component cmp, 
			int nFlagType)
	{
		// get the rules class of the given component
		IRulable rulable = getRulesClassInstance(cmp);

		if (rulable == null)
			return true;

		try
		{
			// decide on which type of attributes
			// should be fetched
			switch (nFlagType)
			{
			/*** Display the component's children onto the 
				     model treeview? ***/
			case FLAG_SHOW_CHILDREN:
				return rulable.showChildren();

				/*** Export the component's children to the ZUML file ***/
			case FLAG_EXPORT_CHILDREN_TO_ZUML:
				return rulable.exportChildrenToZUML();
			}
		}
		catch (Exception e)
		{
		}

		return true;
	}

	/**
	 * Loads the specified component rules from 
	 * the given XML configuration file and returns
	 * a Rules object. 
	 * @param uriConfigXml The URI to the rules 
	 * configuration XML file.
	 * @return The Rules object that contains all 
	 * the predefined component rules
	 */
	public static Rules loadComponentRules(String uriConfigXml)
	{
		if (StringUtils.isEmpty(uriConfigXml))
			return null;

		Configurator config = null;

		try
		{
			// load the configuration file
			config = new Configurator(uriConfigXml);

			// get all the rules entries directly from the
			// iDOM document
			Element[] arrRules = config.getElements("component", null);

			if (ArrayUtils.isEmpty(arrRules))
				return null;

			// create the Rules object
			Rules rules = new Rules();

			// add all the predefined rules
			for (int i = 0; i < arrRules.length; i++)
			{
				// get the component class and the corresponding
				// rules implementation class
				String sClass = arrRules[i].getAttribute("class");
				String sRulesClass = arrRules[i].getAttribute("rulesClass");

				// add the rule
				rules.addRulesClass(sClass, sRulesClass);
			}

			// return the Rules object
			return rules;
		}
		catch (Exception e)
		{
			return null;
		}
		finally
		{
			// clean up
			
			config = null;
		}
	}

	/**
	 * This method resolves the class of 
	 * the given component, fetches its
	 * mapped rules class (if any exists)
	 * and returns a new instance of the 
	 * rules class.  
	 * @param cmp The component to resolve
	 * @return The instance of the mapped
	 * IRulable class
	 */
	private static IRulable getRulesClassInstance(Component cmp)
	{
		if (cmp == null)
			return null;

		// get the component's class
		Class<? extends Component> clazz = cmp.getClass();

		// first check whether the component itself
		// implements the IRuleable interface. If yes, 
		// return the component instance
		if (IRulable.class.isAssignableFrom(clazz))
			return (IRulable) cmp;

		// if not, check the rules map to see if there
		// is any separate rules class specified for this
		// component
		IRulable rule = getMappedRulesClassInstance(cmp.getClass().getName());
		
		// if no map found, return default rules instance
		if ( rule == null )
			return new DefaultRules();
		return rule;
	}

	/**
	 * This method resolves the class of 
	 * the given component, fetches its
	 * mapped rules class (if any exists)
	 * and returns a new instance of the 
	 * rules class.  
	 * @param cmp The component to resolve
	 * @return The instance of the mapped
	 * IRulable class
	 */
	private static IRulable getMappedRulesClassInstance(String sClassName)
	{
		if (StringUtils.isEmpty(sClassName))
			return null;

		// get the pre-loaded Rules object
		Rules rules = DeveloperFactory.getInstance().getRules();

		if (rules == null)
			return null;

		// get the mapped rules class of this component
		// from the Rules object
		String sRulesClass = rules.getRulesClass(sClassName);

		// if the class doesn't have a rules class mapped, exit
		if (StringUtils.isEmpty(sRulesClass))
			return null;

		Class<?> clazzRules = null;

		try
		{
			// get a reference to the rules class (if exists)
			clazzRules = Class.forName(sRulesClass);
		}
		catch (ClassNotFoundException e)
		{
			// if the rules class doesn't exist, exit
			return null;
		}

		// now check if the rules class implements
		// the IRuleable interface
		if (! IRulable.class.isAssignableFrom(clazzRules))
			return null;

		try
		{
			// now that we know that the target class is valid, 
			// create a new instance and execute the correct method
			IRulable rulable = (IRulable) clazzRules.newInstance();

			// return the IRulable instance
			return rulable;
		}
		catch (Exception e)
		{
			return null;
		}
	}
}
