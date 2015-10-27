/**
 * 
 */
package org.sinnlabs.dbvim.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Class that represents search menu
 * @author peter.liverovsky
 *
 */
@DatabaseTable(tableName = "SearchMenus")
public class SearchMenu {
	
	public static final int REFRESH_ONOPEN = 1;
	
	public static final String FORM_FIELD_NAME = "form_id";
	
	public static final String NAME_FIELD_NAME = "name";
	
	@DatabaseField(id = true)
	protected String name;

	@DatabaseField
	protected String qualification;
	
	@DatabaseField(foreign = true, foreignAutoRefresh = true)
	protected Form form;
	
	@DatabaseField
	protected String valueField;
	
	@DatabaseField
	protected String labelField;
	
	@DatabaseField
	protected int refreshPolicy;
	
	public SearchMenu() {
		refreshPolicy = REFRESH_ONOPEN;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getQualification() {
		return qualification;
	}

	public void setQualification(String qualification) {
		this.qualification = qualification;
	}

	public Form getForm() {
		return form;
	}

	public void setForm(Form form) {
		this.form = form;
	}

	public String getValueField() {
		return valueField;
	}

	public void setValueField(String valueField) {
		this.valueField = valueField;
	}

	public String getLabelField() {
		return labelField;
	}

	public void setLabelField(String labelField) {
		this.labelField = labelField;
	}

	public int getRefreshPolicy() {
		return refreshPolicy;
	}

	public void setRefreshPolicy(int refreshPolicy) {
		this.refreshPolicy = refreshPolicy;
	}
}
