/**
 * 
 */
package org.sinnlabs.dbvim.db.exceptions;

/**
 * A generic Exception object that is thrown
 * by the Database engine.
 * @author peter.liverovsky
 *
 */
public class DatabaseOperationException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3684845721861846542L;

	public DatabaseOperationException(Exception e) {
		super(e);
	}
	
	public DatabaseOperationException(String message, Exception e) {
		super(message, e);
	}
}