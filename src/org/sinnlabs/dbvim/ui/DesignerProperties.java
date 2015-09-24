/**
 * 
 */
package org.sinnlabs.dbvim.ui;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.sinnlabs.dbvim.ui.events.PropertiesOnOkEventListener;
import org.sinnlabs.dbvim.zk.model.ComponentFactory;
import org.sinnlabs.dbvim.zk.model.DeveloperFactory;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.IdSpace;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Row;
import org.zkoss.zul.Textbox;

/**
 * @author peter.liverovsky
 *
 */
public class DesignerProperties extends Groupbox implements IdSpace {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1453994780710911695L;

	/**
	 * Current component
	 */
	protected Component current = null;
	
	/**
	 * A map that contains all the Properties values 
	 */
	protected HashMap<Component, String> mapProps = null;

	@Wire("#gridProperties")
	protected Grid gridProperties;

	public DesignerProperties() {
		// create the ui
		Executions.createComponents("/components/ComponentProperties.zul", this, null);
		Selectors.wireComponents(this, this, false);

		setClosable(false);
		
		// Clear properties list
		clear();
	}

	protected void clear() {
		gridProperties.getRows().getChildren().clear();
	}

	/**
	 * Set the component to display the properties
	 * @param comp - reference to the component
	 */
	public void setCurrent(Component comp) {
		current = comp;
		buildProperties();
	}

	protected void buildProperties() {
		if (current == null)
			return;
		
		// clear properties
		clear();
		
		// set the window's title to display the Component's class
		this.setTitle(ComponentFactory.getSimpleClassName(current) + " Properties");

		// get the propery descriptors of the Component class
		PropertyDescriptor[] arrDescriptors = ComponentFactory.getComponentProperties(current);	

		if (ArrayUtils.isEmpty(arrDescriptors))
			return;

		// create a new map to hold all the property values
		mapProps = new HashMap<Component, String>();
		
		// create property event listener
		PropertiesOnOkEventListener listener = new PropertiesOnOkEventListener();

		// loop through the component property descriptors
		for (int i = 0; i < arrDescriptors.length; i++)
		{
			try
			{
				// get the next property descriptor
				PropertyDescriptor descriptor = arrDescriptors[i];

				if (descriptor == null)
					continue;

				// get the name of the object property
				String sName = descriptor.getName();

				Object value = null;

				try
				{
					// get the Property's value
					value = BeanUtils.getProperty(current, descriptor.getName());
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				// create a new Grid row
				Row row = new Row();
				gridProperties.getRows().appendChild(row);

				// add the Property name at the 1st column
				Label lblName = new Label();
				lblName.setValue(sName);
				row.appendChild(lblName);

				// add the Property value at the 2nd column

				// generic Component object
				Component cmpValue = null;

				/*** Boolean Values  ***/
				if (descriptor.getPropertyType() == boolean.class)
				{
					// for boolean properties, display
					// a list box with the 'True' or 'False' 
					// list items
					Listbox list = new Listbox();
					list.setMold("select");
					list.setWidth("90%");
					row.appendChild(list);

					Listitem itemTrue = new Listitem("true");
					list.appendChild(itemTrue);

					Listitem itemFalse = new Listitem("false");
					list.appendChild(itemFalse);

					// display the current property value
					// on the combobox
					boolean bValue = true;

					if (value != null)
						bValue = Boolean.valueOf((String) value).booleanValue();

					if (bValue)
						list.setSelectedItem(itemTrue);
					else
						list.setSelectedItem(itemFalse);

					cmpValue = list;
				}
				else 
				{
					/*** Rest of data types ***/

					// convert the value Object into the correct
					// data type for display
					String sValue = "";

					if (value == null)
						value = "";

					if (value instanceof String)
						sValue = (String) value;
					else 
						sValue = String.valueOf(value);

					// for any other property type, display a 
					// textbox component 
					Textbox textbox = new Textbox();
					textbox.setWidth("95%");
					textbox.setMaxlength(1000);

					// if the size of the value string is > 50 chars, 
					// set the Textbox to multiline, so that the value
					// is visible
					if (sValue.length() > 50)
						textbox.setMultiline(true);

					// display the value on the Textbox
					textbox.setValue(sValue);
					row.appendChild(textbox);

					cmpValue = textbox;
				}

				// add the Textbox objects to the Hashmap, 
				// using their Ids as the key
				mapProps.put(cmpValue, sName);
				cmpValue.addEventListener(Events.ON_OK, listener);
				cmpValue.addEventListener(Events.ON_CHANGE, listener);
				cmpValue.addEventListener(Events.ON_SELECT, listener);
			}
			catch (Exception e)
			{
				continue;
			}
		}// for
	}// buildProperties
	
	public void updateProperty(Component cmp) {

		String propName = mapProps.get(cmp);
		if( StringUtils.isEmpty(propName) ) {
			return;
		}
		// get the descriptor for this property
		PropertyDescriptor descr;
		try {
			descr = PropertyUtils.getPropertyDescriptor(current, propName);
		} catch (IllegalAccessException | InvocationTargetException
				| NoSuchMethodException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		
		// set the new Property values, depending on
		// the property type
		if (cmp.getClass().getSimpleName().equals("Textbox"))
		{
			/*** Textbox property control ***/
			
			// get the property value from the textbox
			String sValue = ((Textbox) cmp).getValue();
			
			// get the property's argument class
			Class<?> clazzProp = descr.getPropertyType();

			// cast the component to its runtime class first
			// and then set its value
			try {
				if ((clazzProp == String.class))
					PropertyUtils.setProperty(current, propName, sValue);
				else if (clazzProp == int.class)
					PropertyUtils.setProperty(current, propName, Integer.valueOf(sValue));
				else if (clazzProp == long.class)
					PropertyUtils.setProperty(current, propName, Long.valueOf(sValue));
			} catch (IllegalAccessException | InvocationTargetException
					| NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (cmp.getClass().getSimpleName().equals("Listbox"))
		{
			/*** Combobox property control ***/

			// get the Listbox
			Listbox list = (Listbox) cmp;
			
			// get the selected item from the Listbox
			Listitem item = list.getSelectedItem();
			
			Boolean bValue = new Boolean((String) item.getLabel());
			
			// set the new value
			try {
				PropertyUtils.setProperty(current, propName, bValue);
			} catch (IllegalAccessException | InvocationTargetException
					| NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		DeveloperFactory.getInstance().getSynchronizer().synchronizeTreeWithCanvas(
				DeveloperFactory.getInstance().getDesignerCanvas());
	}
}