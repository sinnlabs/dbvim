/**
 * 
 */
package org.sinnlabs.dbvim.model;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.sinnlabs.dbvim.config.ConfigLoader;
import org.sinnlabs.dbvim.db.model.DBModel;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.mysql.jdbc.StringUtils;

/**
 * Class that represents View for db table
 * @author peter.liverovsky
 *
 */
@DatabaseTable(tableName = "dbForms")
public class Form implements IForm {
	
	public static final String NAME_FIELD_NAME = "name";
	public static final String CONNECTION_FIELD_NAME = "connection_id";
	
	protected DBModel dbModel = null;
	
	@DatabaseField(id = true)
	protected String name;
	
	@DatabaseField(canBeNull = false, foreign = true, foreignAutoRefresh = true)
	protected DBConnection connection;
	
	@DatabaseField
	protected String dbTableName;
	
	@DatabaseField
	protected String catalogName;
	
	@DatabaseField(width = 1024*1024, dataType = DataType.SERIALIZABLE)
	protected ArrayList<ResultColumn> resultList;
	
	@DatabaseField(width = 7999)
	protected String view;
	
	@DatabaseField
	protected String title;
	
	@DatabaseField
	protected boolean isJoin = false;
	
	@DatabaseField
	protected boolean isOuterJoin = false;
	
	@DatabaseField(foreign = true, foreignAutoRefresh = true)
	protected Form leftForm;
	
	@DatabaseField(foreign = true, foreignAutoRefresh = true)
	protected Form rightForm;
	
	@DatabaseField
	protected String joinCondition;
	
	
	/* Getters and Setters */
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
	public DBConnection getDBConnection() { return connection; }
	public void setDBConnection(DBConnection conn) { connection = conn; }
	public String getTableName() { return dbTableName; }
	public void setTableName(String tableName) { dbTableName = tableName; }
	public String getCatalog() { return catalogName; }
	public void setCatalog(String catalog) { catalogName = catalog; }
	public String getView() { return view; }
	public void setView(String view) { this.view = view; }
	public List<ResultColumn> getResultList() { return resultList; }
	public void setResultList(ArrayList<ResultColumn> sResultList) { resultList = sResultList; }
	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }
	public boolean isJoin() { return isJoin; }
	public void setJoin(boolean join) { isJoin = join; }
	public boolean isOuterJoin() { return isOuterJoin; }
	public void setOuterJoin(boolean outer) { isOuterJoin = outer; }
	public Form getLeftForm() throws SQLException { 
		refresh(leftForm);
		return leftForm;
	}
	public void setLeftForm(Form form) { leftForm = form; }
	public Form getRightForm() throws SQLException { 
		refresh(rightForm);
		return rightForm;
	}
	public void setRigthForm(Form form) { rightForm = form; }
	public String getJoinClause() { return joinCondition; }
	public void setJoinClause(String clause) { joinCondition = clause; }
	
	private void refresh(Form f) throws SQLException {
		ConfigLoader.getInstance().getForms().refresh(f);
		ConfigLoader.getInstance().getDBConnections().refresh(f.connection);
	}
	
	public String getQualifiedName() {
		String res = dbTableName;
		if ( !StringUtils.isNullOrEmpty(catalogName) ) {
			res = catalogName + "." + res;
		}
		return res;
	}
	
	@Override
	public String toString() {
		return dbTableName + "(" + catalogName + ")";
	}	
}