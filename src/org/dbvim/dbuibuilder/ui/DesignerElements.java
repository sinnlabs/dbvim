/**
 * 
 */
package org.dbvim.dbuibuilder.ui;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.ArrayUtils;
import org.dbvim.dbuibuilder.config.Configurator;
import org.dbvim.dbuibuilder.zk.model.ElementInfo;
import org.xml.sax.SAXException;
import org.zkoss.idom.Element;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Div;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Tab;
import org.zkoss.zul.Tabbox;
import org.zkoss.zul.Tabpanel;

/**
 * UI module that displays accordion tabs
 * that shows designer toolkit
 * @author peter.livarovsky
 *
 */
public class DesignerElements extends Div implements IdSpace {

	/**
	 * 
	 */
	private static final long serialVersionUID = -9190617513272944386L;
	
	/**
	 * The Configurator instance used for loading
	 * configuration preferences 
	 */
	private static Configurator config = null;
	
	/**
	 * The total number of component groups 
	 * defined in the configuration file 'toolkit.xml' 
	 */
	private int nGroupNum = 0;
	
	@Wire("#tabbox")
	Tabbox tabbox;
	
	public static Configurator getComponentsConfigurator() { return config; }
		
	public DesignerElements() {
		//super();
		// create the ui
		Executions.createComponents("/components/ElementList.zul", this, null);
		Selectors.wireComponents(this, this, false);
		
		// load the 'toolkit.xml' configuration file
		try {
			config = new Configurator(
					Executions.getCurrent().getDesktop().getWebApp().getRealPath(
							"/config/toolkit/toolkit.xml"));
		} catch (SAXException | IOException | ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		createTabs();
	}
	
	/**
	 * Creates the Accordion style Tabs container 
	 */
	protected void createTabs()
	{
		if (config == null)
			return;
		
		try
		{
			// get the defined tab groups
			Element[] arrGroups = config.getElements("tab", null);
			
			if (ArrayUtils.isEmpty(arrGroups))
				return;
			
			// get the total number of component groups
			nGroupNum = arrGroups.length;
			
			// create the tab groups
			createComponentTabs(arrGroups);
		}
		catch (Exception e)
		{
			// if the configuration file could not be loaded, exit
			e.printStackTrace();
			return;
		}
	}
	
		
	/**
	 * Creates a components tabbox within the 
	 * group for the specified element. 
	 * @param group
	 * @param panel
	 */
	protected void createComponentTabs(Element[] arrTabs)
	{
		if ((config == null) || (arrTabs == null))
			return;
		
		
		// create the Tabs to accomodate the tab groups
		for (int i = 0; i < arrTabs.length; i++)
		{
			// get the next 'group' iDOM Element
			Element elementTab = arrTabs[i];

			// create the tab that displays the group name
			Tab tab = new Tab(elementTab.getAttribute("name"));
			
			
			tabbox.getTabs().appendChild(tab);
			
			// create a panel for the group tab
			Tabpanel panel = new Tabpanel();
			
			
			tabbox.getTabpanels().appendChild(panel);
			
			// add the predefined visual components to the tab
			addComponentsToTab(elementTab, panel);
		}
	}
	
	/**
	 * Retrieves the predefined visual components
	 * declared within the iDOM and attaches them 
	 * onto the specified tab panel 
	 * @param elementTab The iDOM description of the
	 * tab element
	 * @param tabpanel The panel where the components 
	 * will be attached to
	 */
	private void addComponentsToTab(Element domTab, 
								    Tabpanel tabpanel)
	{
		if ((config == null) || (domTab == null) || (tabpanel == null))
			return;
	
		// get the components defined for this tab, 
		// directly from the iDOM document
		Element[] arrComponents = config.getElements("component", domTab);
		
		if (ArrayUtils.isEmpty(arrComponents))
			return;
		// create the list with components items
		Listbox list = new Listbox();
		list.setWidth("100%");
		list.setHflex("1");
		list.setVflex("1");
		list.setDroppable("false");
		
		// create the Tabs to accomodate the tab groups
		for (int i = 0; i < arrComponents.length; i++)
		{
			try
			{
				// get the next 'group' iDOM Element
				Element domComponent = arrComponents[i];
	
				// get the <class>, <image> and <tooltip> values
				// directly from the iDOM document
				Element domClass = config.getElement("class", domComponent);
				Element domImage = config.getElement("image32", domComponent);
				Element domTooltip = config.getElement("tooltip", domComponent);
				Element domName = config.getElement("name", domComponent);
				
				if (domClass == null)
					continue;
				
				// create the component's image and attach it onto
				// the panel, according to its iDOM description
				ElementInfo component = new ElementInfo(domClass.getText(), 
						domName.getText(), domTooltip.getText(), domImage.getText());
				ComponentListItem item = new ComponentListItem(component);
				list.getItems().add(item);
			}
			catch (Exception e)
			{
				// if something is missing, just move on
				continue;
			}
		}
		tabpanel.appendChild(list);
	}
}
