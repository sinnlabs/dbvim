package org.sinnlabs.dbvim.rules.engine;

import java.util.HashMap;

import org.apache.commons.lang3.StringUtils;
import org.sinnlabs.dbvim.zk.IDisposable;

/**
 * A class that holds all the rules
 * that are defined in the 'rules.xml' 
 * configuration file.
 * @author chris.spiliotopoulos
 *
 */
public class Rules implements IDisposable
{
	/**
	 * A map that holds all the rules classes
	 * that are mapped to individual components
	 */
	private HashMap<String, String> _mapRuleClasses = null;
	
	/**
	 * Adds a rules class for the specified 
	 * component class.
	 * @param sComponentClass The component's canonical
	 * class name
	 * @param sRulesClass The corresponding class name
	 * that implements the component's rules
	 */
	public void addRulesClass(String sComponentClass, 
							  String sRulesClass)
	{
		if (_mapRuleClasses == null)
			_mapRuleClasses = new HashMap<String, String>();
		
		if ((StringUtils.isEmpty(sComponentClass)) ||
			(StringUtils.isEmpty(sRulesClass)))	
			 return;
		
		try
		{
			// add the rule class to the map
			_mapRuleClasses.put(sComponentClass, sRulesClass);
		}
		catch (Exception e)
		{
		}
	}
	
	/**
	 * Returns the rules class that is mapped
	 * for the given component class.
	 * @param sComponentClass The component's 
	 * canonical class name
	 * @return The assigned rules class canonical 
	 * name
	 */
	public String getRulesClass(String sComponentClass)
	{
		if ((_mapRuleClasses == null)  || 
		    (StringUtils.isEmpty(sComponentClass)))
			return "";
		
		// get the rules class from the map
		Object sRulesClass = _mapRuleClasses.get(sComponentClass);
		
		if (sRulesClass == null)
			return "";
		
		// return the rules class
		return (String) sRulesClass;
	}

	/* (non-Javadoc)
	 * @see com.zk.designer.IDisposable#dispose()
	 */
	public void dispose()
	{
		// clean up
		_mapRuleClasses = null;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#finalize()
	 */
	protected void finalize() throws Throwable
	{
		try
		{
			// clean up
			dispose();
		}
		catch (Exception e)	{ }
		finally
		{
			super.finalize();
		}
	}
}
