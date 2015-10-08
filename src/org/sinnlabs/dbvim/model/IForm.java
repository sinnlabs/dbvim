/**
 * 
 */
package org.sinnlabs.dbvim.model;

/**
 * Interface represents basic form
 * @author peter.liverovsky
 *
 */
public interface IForm {

	public String getName();
	
	public void setName(String name);
	
	public DBConnection getDBConnection();
	
	public void setDBConnection(DBConnection conn);
	
	public String getView();
	
	public void setView(String view);
	
	public String getTitle();
	
	public void setTitle(String title);
}
