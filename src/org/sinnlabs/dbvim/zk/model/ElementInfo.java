package org.sinnlabs.dbvim.zk.model;

import java.util.List;

/**
 * 
 * @author peter.liverovsky
 *
 */
public class ElementInfo {

	private String className;
	private String Name;
	private String helpText;
	private String imageUrl;
	private List<String> events;
	
	public ElementInfo() {
		className = "";
		Name = "";
		helpText = "";
		imageUrl = "";
	}
	
	public ElementInfo(String clazz, String name, String help, String image) {
		className = clazz;
		Name = name;
		helpText = help;
		imageUrl = image;
	}

	/**
	 * Returns class name of the component
	 * @return fqdn class name
	 */
	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public String getHelpText() {
		return helpText;
	}

	public void setHelpText(String helpText) {
		this.helpText = helpText;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public List<String> getEvents() {
		return events;
	}

	public void setEvents(List<String> events) {
		this.events = events;
	}
}
