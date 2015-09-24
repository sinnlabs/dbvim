/**
 * 
 */
package org.sinnlabs.dbvim.db.model;

/**
 * @author peter.liverovsky
 *
 */
public class DBTable {
	private String name;
	
	private String catalog;
	
	public DBTable() {
		
	}
	
	public DBTable(String catalog, String name) {
		this.name = name;
		this.catalog = catalog;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCatalog() {
		return catalog;
	}

	public void setCatalog(String catalog) {
		this.catalog = catalog;
	}
}
