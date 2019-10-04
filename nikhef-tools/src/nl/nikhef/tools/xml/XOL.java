/* *****************************************************************************
 * SaFariPark SFP+ editor and support libraries
 * Copyright (C) 2017 National Institute for Subatomic Physics Nikhef
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package nl.nikhef.tools.xml;

import java.io.CharArrayReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * XOL is the Xml to Object Loader. A simple XML parser which tries to instantiate
 * classes bound to tags.
 * 
 * @author vincent
 */
public class XOL 
{

	private Map<String, XOLProxyFactory<?>> _tagMapping = new HashMap<String, XOLProxyFactory<?>>();
	
	private Map<String, XOLProxy<?>> _idToProxy = new HashMap<String, XOLProxy<?>>();
	
	public <I extends Object> void setMapping(String tagName, XOLProxyFactory<I> f)
	{
		_tagMapping.put(tagName, f);
	}
	
	public <I extends Object> XOLProxyFactory<I> setMapping(String tagName, Class<I> clazz) {
		XOLProxyFactory<I> f = new SimpleObjectProxyFactory<I>(clazz);
		setMapping(tagName, f);
		return f;
	}

	public List<? extends Object> parseString(String string) throws XMLStreamException {
		CharArrayReader reader = new CharArrayReader(string.toCharArray());
		List<? extends Object> objs = loadObject(reader);
		reader.close();
		return objs;
	}
	
	private static class RootProxy extends XOLProxyBase<List<Object>>
	{
		List<Object> _rootObjects = new ArrayList<Object>();
		
		@Override
		public void addChild(XOLProxy<?> obj) {
			_rootObjects.add(obj.getInstance());
		}

		@Override
		public List<Object> getInstance() {
			return _rootObjects;
		}
		
		public String getTypeName() {
			return "XOL Root";
		}

		@Override
		public void complete() {
		}

		
	}
	
	public List<? extends Object> loadObject(FXML fxml)
	{

		XOLProxy<List<Object>> root = new RootProxy();
		root.setXOL(this);
		
				
		fxmlToProxy(root, fxml.getRoot());
		
		return root.getInstance();
	}
	
	
	private XOLProxy<?> fxmlToProxy(XOLProxy<?> parent, FXMLElement fxml) 
	{
		
		
		if (!_tagMapping.containsKey(fxml.tag))
			throw new RuntimeException("Tag '" + fxml.tag + "' unknown");
		
		// First get proxy
		XOLProxy<?> proxy = _tagMapping.get(fxml.tag).newProxy();
		proxy.setXOL(this);
		proxy.setParent(parent);
		
		// Then, set attributes
		for (Map.Entry<String, String> att : fxml.attributes().entrySet())
		{
			// ignore namespaces
			if (att.getKey().contains(":")) continue;
			
			if (att.getKey().equals("id")) {
				if (_idToProxy.containsKey(att.getValue())) {
					throw new RuntimeException(String.format("An entity with ID '%s' already exists", att.getValue()));
				}
				_idToProxy.put(att.getValue(), proxy);							
			}

			if (!proxy.setAttribute(att.getKey(), att.getValue())) {
				throw new RuntimeException("Attribute '" + att.getKey() + "' for '" 
						+ proxy.getTypeName() + "' can not be set"); 
				
			}
		}
		
		// check for mixed mode
		if (fxml.hasChars() && fxml.hasChildren()) {
			throw new RuntimeException("Mixed XML content is not allowed");
		}
		
		// if its only chars
		if (fxml.hasChars())
		{
			proxy.setContent(fxml.getChars());
		} else if (fxml.hasChildren()) 
		{
			for(FXMLElement child : fxml.children())
			{
				fxmlToProxy(proxy, child);
			}
		}
		// add child to parent
		parent.addChild(proxy);
		// signal completion
		proxy.complete();
		return proxy;
	}

	public List<? extends Object> loadObject(Reader r) throws XMLStreamException
	{
		
		LinkedList<XOLProxy<?>> stack = new LinkedList<XOLProxy<?>>();
		
		XMLInputFactory f = XMLInputFactory.newInstance();
		XMLEventReader er = f.createXMLEventReader(r);
		
		XOLProxy<List<Object>> root = new RootProxy();
		root.setXOL(this);
	
		XOLProxy<? extends Object> current = root;
		XOLProxy<?> parent = null;
		
		while(er.hasNext()) {
			XMLEvent event = er.nextEvent();
			
			switch (event.getEventType())
			{
			case XMLEvent.START_ELEMENT:
			{
				StartElement se = event.asStartElement();
				String name = se.getName().toString();
				if (!_tagMapping.containsKey(name))
					throw new RuntimeException("Tag '" + name + "' unknown");
				
				stack.push(current);
				parent = current;
				current = _tagMapping.get(name).newProxy();
				current.setXOL(this);
				
				current.setParent(parent);
				
				
				Iterator<?> it = se.getAttributes();
				while (it.hasNext()) 
				{
					Attribute attr = (Attribute)it.next();
					String attName = attr.getName().toString();
					String attValue = attr.getValue();
					
					if (attName.equals("id")) {
						if (_idToProxy.containsKey(attValue)) {
							throw new RuntimeException(String.format("An entity with ID '%s' already exists", attValue));
						}
						_idToProxy.put(attValue, current);							
					}
					
					if (!current.setAttribute(attName, attValue)) {
						throw new RuntimeException("Attribute '" + attName + "' for '" 
								+current.getTypeName() + "' can not be set"); 
					}
				}
				
			}
			break;
			case XMLEvent.CDATA:
			case XMLEvent.CHARACTERS:
				Characters chars = event.asCharacters();
				
				String result = chars.getData().trim();
				if (result.length() == 0) break;
				
				if (!current.setContent(chars.getData())) {
					throw new RuntimeException("Characters for '" 
							+current.getTypeName() + "' can not be set"); 
				}
				
			break;
			case XMLEvent.END_ELEMENT:
			{
				parent = stack.pop();
				parent.addChild(current);
				current.complete();
				current = parent;
				parent = stack.peek();
			}
			break;
			case XMLEvent.END_DOCUMENT:
			case XMLEvent.START_DOCUMENT:
			case XMLEvent.COMMENT: 
				break;
			default:
				System.out.printf("Unprocessed event! %s (type=%^s)\n", event, event.getClass().getSimpleName());
			}
			
		}		
		return root.getInstance();
	}

	public static final String beanifyProperty(String string) {
		
		StringBuilder attOut = new StringBuilder();
		
		boolean capNext = false;
		
		for (char c : string.toCharArray()) 
		{
			if (Character.isLetterOrDigit(c)) {
				
				if (capNext) 
				{
					c = Character.toUpperCase(c);
					capNext = false;
				}
				
				attOut.append(c);
			} else 	capNext = true;
		}
		
		return attOut.toString();
	}
	
	public XOLProxy<?> getProxyById(String id) {
		if (_idToProxy.containsKey(id)) return _idToProxy.get(id);
		return null;
	}

	public static int parseInt(String val) {
		
		if (val.startsWith("x")) {
			return Integer.parseInt(val.substring(1), 16);
		}
		return Integer.parseInt(val);
	}
	

}
