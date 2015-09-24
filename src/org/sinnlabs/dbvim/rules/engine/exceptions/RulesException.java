package org.sinnlabs.dbvim.rules.engine.exceptions;

import org.sinnlabs.dbvim.rules.engine.RulesResult;

/**
 * A generic Exception object that is thrown
 * by the rules engine.
 * @author chris.spiliotopoulos
 *
 */
public class RulesException extends Exception
{
	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * The rules engine result object 
	 */
	private RulesResult _result = null;
	
	/* (non-Javadoc)
	 * @see java.lang.Throwable#getMessage()
	 */
	public String getMessage()
	{
		if (_result == null)
			return "Unspecified rules error...";
		
		return _result.getMessage();
	}

	/**
	 * Returns the rules operation result code
	 */
	public int getResultCode()
	{
		if (_result == null)
			return RulesResult.ERR_UNSPECIFIED;
		
		return _result.getResultCode();
	}
	
	/**
	 * Constructor
	 * @param result The rules operation result object
	 */
	public RulesException(RulesResult result)
	{
		_result = result;
	}
}
