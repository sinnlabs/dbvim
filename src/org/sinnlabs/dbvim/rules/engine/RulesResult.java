package org.sinnlabs.dbvim.rules.engine;

/**
 * A class that represents a result
 * object returned from the Rules Engine, 
 * as soon as a set of rules is applied to 
 * a component.
 * @author chris.spiliotopoulos
 *
 */
public class RulesResult
{
	/**
	 * The operation was successful 
	 */
	public static final int SUCCESS = 0;
		
	/**
	 * An unspecified error occured 
	 */
	public static final int ERR_UNSPECIFIED = 1;
	
	/**
	 * The result code 
	 */
	private int _nResultCode = SUCCESS;
	
	/**
	 * The result message 
	 */
	private String _sMessage = "";
	
	/**
	 * Constructor
	 * @param nResult The result code
	 * @param sMessage The result message
	 */
	public RulesResult(int nResult, 
					   String sMessage)
	{
		if (nResult < SUCCESS)
			nResult = ERR_UNSPECIFIED;
		
		_nResultCode = nResult;
		_sMessage = sMessage;
	}
	
	/**
	 * Returns the result code of the rule set operation
	 */
	public int getResultCode() { return _nResultCode; }

	/**
	 * Returns the result message
	 */
	public String getMessage() { return _sMessage; }

}
