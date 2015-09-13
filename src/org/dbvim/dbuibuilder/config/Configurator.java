/**
 * 
 */
package org.dbvim.dbuibuilder.config;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.xml.sax.SAXException;
import org.zkoss.idom.Attribute;
import org.zkoss.idom.Document;
import org.zkoss.idom.Element;
import org.zkoss.idom.Item;
import org.zkoss.idom.input.SAXBuilder;

/**
 * @author peter.liverovsky
 *
 */
public class Configurator {
	/**
	 * The iDOM document 
	 */
	private Document _doc = null;
	
	/**
	 * The requested iDOM element 
	 */
	private Element _locatedElement = null;
	
	/**
	 * An array of located iDOM Elements
	 */
	private Element[] _arrElements = null;
	
	/**
	 * @param uri
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	public Configurator(String uri) throws SAXException, 
										   IOException, 
										   ParserConfigurationException
	{
		// load the configuration file
		loadConfigurationFile(uri);
	}
	
	/**
	 * Loads the specified configuration file
	 * @param uri The URI to the file
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 * @throws SAXException 
	 */
	private void loadConfigurationFile(String uri) throws SAXException, 
														  IOException, 
														  ParserConfigurationException
	{
		// try to load the XML configuration file and
		// create the iDOM document
		_doc = new SAXBuilder(false, false, true).build(uri);
	}
	
	/**
	 * Searches the iDOM document for the
	 * specified element and returns the
	 * first one encountered.
	 * @param sTagName The name of the tag 
	 * to be retrieved
	 * @return The requested Element
	 */
	public Element getElement(String sTagName, 
							  Element rootElement)
	{
		_locatedElement = null;
		_arrElements = null;
		
		if (_doc == null)
			return null;
		
		if (rootElement == null)
			locateElement(_doc.getRootElement(), sTagName, null, null, null, false);
		else
			locateElement(rootElement, sTagName, null, null, null, false);
		
		// return an array of the Elements found
		return _locatedElement;
	}
	
	/**
	 * Searches the iDOM document for the
	 * specified element and returns the
	 * first one encountered.
	 * @param sTagName The name of the tag 
	 * to be retrieved
	 * @return The requested Element
	 */
	public Element[] getElements(String sTagName, 
								 Element rootElement)
	{
		_locatedElement = null;
		_arrElements = null;
		
		if (_doc == null)
			return null;
		
		if (rootElement == null)
			locateElement(_doc.getRootElement(), sTagName, null, null, null, true);
		else
			locateElement(rootElement, sTagName, null, null, null, true);
		
		// return an array of the Elements found
		return _arrElements;
	}
	
	/**
	 * Searches the iDOM document for the
	 * specified element and returns the
	 * first one encountered.
	 * @param sTagName The name of the tag 
	 * to be retrieved
	 * @return The requested Element
	 */
	public Element getElement(String sTagName, 
							  String sMatchingTextValue,
							  Element rootElement)
	{
		_locatedElement = null;
		_arrElements = null;
		
		if (_doc == null)
			return null;
		
		if (rootElement == null)
			locateElement(_doc.getRootElement(), sTagName, null, null, sMatchingTextValue, false);
		else
			locateElement(rootElement, sTagName, null, null, sMatchingTextValue, false);
		
		// return an array of the Elements found
		return _locatedElement;
	}
	
	/**
	 * Walks through the Document model
	 * and performs an element-by-element 
	 * search in using the specified matching
	 * attribute 
	 * @param sAttributeName The Attribute to search for
	 * @param tree The matching Attribute value
	 */
	protected Element locateElement(Element domElement,
									String sTagName,
									String sAttributeName,
								    String sAttributeValue,
								    String sMatchingTextValue,
								    boolean bAllInstances)
	{
		if (_locatedElement != null)
			return _locatedElement;
		
		if (domElement == null)
			return null;

		if (sTagName == null)
			return null;
		
		if ((! StringUtils.isEmpty(sAttributeName)) &&
			(! StringUtils.isEmpty(sAttributeValue)))	 
		{
			// get the specified Attribute
			Attribute domAttribute = domElement.getAttributeItem(sAttributeName);
			
			if (domAttribute != null)
			{	
				// check if we have an Attribute value match
				if (domAttribute.getValue().equals(sAttributeValue))
				{
					if (! bAllInstances)
					{
						/*** Perform Text value matching ***/
						if (! StringUtils.isEmpty(sMatchingTextValue))
						{
							// if the element's text value matches the 
							// specified criterion, return it and exit
							if (domElement.getText().equalsIgnoreCase(sMatchingTextValue))
								_locatedElement = domElement;
								return domElement;
						}
						else
						{
							_locatedElement = domElement;
							return domElement;
						}
					}
					else
					{
						// add the Element to the array
						ArrayUtils.add(_arrElements, domElement);
					}
				}
			}
		}
		else
		{
			// check only the tag name
			if (domElement.getName().equals(sTagName))
			{
				if (! bAllInstances)
				{
					/*** Perform Text value matching ***/
					if (! StringUtils.isEmpty(sMatchingTextValue))
					{
						// if the element's text value matches the 
						// specified criterion, return it and exit
						if (domElement.getText().equalsIgnoreCase(sMatchingTextValue))
							_locatedElement = domElement;
							return domElement;
					}
					else
					{
						_locatedElement = domElement;
						return domElement;
					}
				}
				else
				{
					// add the Element to the array
					_arrElements = (Element[]) ArrayUtils.add(_arrElements, domElement);
				}
			}
		}
		
		// get the component's children
		List<Item> listChildren = domElement.getChildren();

		if ((listChildren == null) || (listChildren.size() == 0))
			return null;
		
		// loop through all the component's children
		Iterator<Item> iter = listChildren.iterator();
		while (iter.hasNext())
		{
			// get the next component in the list
			Object child = iter.next();
			
			if (! (child instanceof Element))
				continue;

			// parse the model of the child
			locateElement((Element) child, sTagName, sAttributeName, sAttributeValue, sMatchingTextValue, bAllInstances);
		}
		
		return null;
	}
}
