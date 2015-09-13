package org.dbvim.dbuibuilder.zk.model;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.dbvim.dbuibuilder.config.Configurator;
import org.dbvim.dbuibuilder.rules.engine.RulesEngine;
import org.dbvim.dbuibuilder.rules.engine.exceptions.DisplayableRulesException;
import org.dbvim.dbuibuilder.rules.engine.exceptions.RulesException;
import org.dbvim.dbuibuilder.ui.DesignerElements;
import org.zkoss.idom.Element;
import org.zkoss.lang.Classes;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.sys.DesktopCtrl;
import org.zkoss.zul.Messagebox;

public class ComponentFactory {
	/**
	 * Creates a new UI Component of the given class.
	 * @param cls The component's class
	 * @return The newly created component with default 
	 * properties
	 * @throws Exception 
	 */
	
	public static Component createComponent(String sComponentClass) throws Exception
	{
		if (StringUtils.isEmpty(sComponentClass))
			return null;
		
		// get an instance of the given component class
		Class<?> componentClass = Class.forName(sComponentClass);

		// create a new instance of the specified component class
		Component newComponent = (Component) componentClass.newInstance();

		try
		{
			// check for any pre-creation rules
			RulesEngine.applyRules(newComponent, RulesEngine.PRE_CREATION_RULES);
		}
		catch (RulesException re)
		{
			// if a rules exception has been thrown, 
			// do not create the component on canvas
			newComponent = null;	
			
			// if this is a displayable exception, 
			// display the message on a Messagebox.
			// display an error message and exit
			if (re instanceof DisplayableRulesException)
			{
				Messagebox.show(re.getMessage(), "Rules Exception", Messagebox.OK, Messagebox.ERROR);
			}
			
			return null;
		}
		
		Method method = null;

		try
		{
			// check if the component implements 'onCreate' method
			method = Classes.getAnyMethod(componentClass, "onCreate", null);

			// if yes, invoke it now to do custom initialization
			method.invoke(newComponent, (Object[])null);
		}
		catch (NoSuchMethodException e)
		{
		}
		
		// assign the component Id
		newComponent.setId(fixAutoId(newComponent.getUuid()));
			
		// return the newly created component
		return newComponent;
	}
	
	/**
	 * Returns the simple name of a component's class.
	 * @param clazz The component class to resolve
	 */
	public static String getSimpleClassName(Component cmp)
	{
		if (cmp == null)
			return "";
		
		// get the component's implementation class
		Class<? extends Component> clazz = cmp.getClass();
		
		if (clazz == null)
			return "";
		
		return clazz.getSimpleName();
	}
	
	/**
	 * Converts an auto-generated component 
	 * Id assigned from the framework into
	 * a valid one for later re-loading.
	 * @param sId component's Id
	 * @return A new valid Id
	 */
	public static String fixAutoId(String sId)
	{
		if (StringUtils.isEmpty(sId))
			return "";
		
		// if the given Id is not auto-generated, 
		// do not change it
		if (! isAutoId(sId))
			return sId;
	
		String sNewId = "";
		
		// the easiest way to convert an auto-generated
		// Id into a new valid one, is by removing the
		// leading 'z_' chars and using something else.
		sNewId = StringUtils.removeStart(sId, "z_");
		sNewId = "id_" + sNewId;
		
		// return fixed Id
		return sNewId;
	}
	
	/**
	 * Walks through the given component model and 
	 * assigns new Ids to all the contained elements.
	 * @param element
	 * @return
	 */
	public static Component assignNewIds(Component element)
	{
		if (element == null)
			return null;
		
		// assign a new Id to the component
		assignNewId(element);
		
		return element;
	}

	public static void assignNewId(Component component) {
		// TODO Generate new custom ID
		// get the next available component Id for current Desktop	
		
		if (component == null)
			return;
		
		// assign a new Id to the given component
		component.setId(createId(component));
		
		// fix auto id
		component.setId(fixAutoId(component.getId()));
		
		// get component's children
		List<Component> listChildren = component.getChildren();

		if (listChildren.size() == 0)
			return;
		
		// loop through all component's children
		Iterator<Component> iter = listChildren.iterator();
		while (iter.hasNext())
		{
			try
			{
				// get next component in the list
				Component child = (Component) iter.next();
				
				if (child == null)
					continue;
				
				/*** RECURSION ***/
				assignNewId(child);
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}
	
	protected static String createId(Component comp) {
		return ((DesktopCtrl) Executions.getCurrent().getDesktop()).getNextUuid(comp);
	}
	
	/**
	 * Returns all the valid properties of the component
	 * that should be displayed on Property view.
	 * @param cmp component to resolve
	 * @return PropertyDescriptor array
	 */
	public static PropertyDescriptor[] getComponentProperties(Component cmp)
	{
		if (cmp == null)
			return null;
		
		// get property descriptors of the Component class
		PropertyDescriptor[] arrDescriptors = PropertyUtils.getPropertyDescriptors(cmp);	
		
		if (ArrayUtils.isEmpty(arrDescriptors))
			return null;
		
		// get the list of the component's properties
		// that should not be displayed onto the view
		String[] arrExcludedProps = RulesEngine.getComponentAttributes(
				cmp, RulesEngine.ATTRIBUTES_EXCLUDE_FROM_PROPERTY_VIEW);

		PropertyDescriptor[] arrValidProps = null;
		
		// loop through the component property descriptors
		for (int i = 0; i < arrDescriptors.length; i++)
		{
			try
			{
				// get the next property descriptor
				PropertyDescriptor descriptor = arrDescriptors[i];
				
				if (descriptor == null)
					continue;
				
				// get read / write property methods
				Method methodRead = descriptor.getReadMethod();
				Method methodWrite = descriptor.getWriteMethod();
				
				// for all properties, both read and write
				// methods should exist
				if ((methodRead == null) ||
					(methodWrite == null))
					continue;
				
				// get the name of the object property
				String sName = descriptor.getName();
				
				// check the list of excluded properties to see
				// if this property is banned by the rules
				if (! ArrayUtils.isEmpty(arrExcludedProps))
				{
					// if yes, move to the next property
					if (ArrayUtils.contains(arrExcludedProps, sName))
						continue;
				}
				
				// Filter out the following properties, 
				// as they are used internally:
				//
				//  * childable
				//  * zIndexByClient
				//  * innerAttrs
				//  * leftByClient
				//  * topByClient
				//  * outerAttrs
				//  * transparent
				if (sName.equalsIgnoreCase("childable") || 
					sName.equalsIgnoreCase("zIndexByClient") || 
					sName.equalsIgnoreCase("innerAttrs") ||
					sName.equalsIgnoreCase("leftByClient") ||
					sName.equalsIgnoreCase("topByClient") ||
					sName.equalsIgnoreCase("outerAttrs") ||
					sName.equalsIgnoreCase("transparent"))
					continue;
				
				// Display only properties of the following types
				//
				// * String
				// * String[]
				// * int
				// * boolean
				// * long
				if (((descriptor.getPropertyType() != String.class) && 
					 (descriptor.getPropertyType() != String[].class) &&	
					 (descriptor.getPropertyType() != boolean.class) && 
					 (descriptor.getPropertyType() != int.class) &&
					 (descriptor.getPropertyType() != long.class)) ||
					(descriptor.isHidden())
					)	
				{	
					continue;
				}
				
				if (arrValidProps == null)
					arrValidProps = new PropertyDescriptor[]{};

				// this is a valid property, so add it to the array
				arrValidProps = (PropertyDescriptor[]) ArrayUtils.add(arrValidProps, descriptor);
			}
			catch (Exception e)
			{
				continue;
			}
		}

		// dispose
		arrDescriptors = null;
		
		// return the array of valid properties
		return arrValidProps;
	}
	
	/**
	 * In ZK version 7.0.3 we can not determine which id is auto generated.
	 * In this situation we check for leading 'z_' prefix
	 * @param id - Component id that needs to be checked
	 * @return
	 */
	public static boolean isAutoId(String id) {
		if (id.startsWith("z_"))
				return true;
		return false;
	}
	
	/**
	 * Get component events to be displayed
	 * @param clzz Component class
	 * @return Array of event names
	 */
	public static String[] getComponentEvents(Class<?> clazz)
	{
		
		if ((clazz == null))
			return null;
		
		// get canonical name of the component's class
		String sClassName = clazz.getName();
		
		// get component's Configurator instance
		// from the toolkit object
		Configurator config = DesignerElements.getComponentsConfigurator();
		
		if (config == null)
			return null;
		
		// first try to locate the component element within
		// the configuration iDOM
		Element domClass = config.getElement("class", sClassName, null); 
		
		if (domClass == null)
			return null;
		
		// if a <class> element was found, get 
		// its parent which is the <component> element
		Element domComponent = (Element) domClass.getParent();
		
		if (domComponent == null)
			return null;
		
		// get the required <events> tag from the node
		Element domImage = config.getElement("events", domComponent);
		
		if (domImage == null)
			return null;
		
		// get component's event list
		String sEventList = domImage.getText();
		
		if (StringUtils.isEmpty(sEventList))
			return null;
		
		// split the event list into an array
		// of events
		String[] arrEvents = StringUtils.split(sEventList, ',');
	
		// return the events array
		return arrEvents;
	}
}
