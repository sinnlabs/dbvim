package org.dbvim.dbuibuilder.zk;

/**
 * Implemented by objects that
 * take care of their own disposal
 *
 */
public interface IDisposable
{
	/**
	 * Enforces an object to clean-up itself 
	 */
	public void dispose();
}