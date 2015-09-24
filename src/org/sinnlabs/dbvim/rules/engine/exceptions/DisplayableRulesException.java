package org.sinnlabs.dbvim.rules.engine.exceptions;

import org.sinnlabs.dbvim.rules.engine.RulesResult;


/**
 * A rules engine exception that its message
 * should be displayed on a popup message box
 * @author chris.spiliotopoulos
 *
 */
public class DisplayableRulesException extends RulesException
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 4271773547433554663L;
	
	/**
	 * Constructor
	 * @param result
	 */
	public DisplayableRulesException(RulesResult result)
	{
		super(result);
	}
}