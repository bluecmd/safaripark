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

import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class FXML {

	private FXMLElement _root;
	
	
	public FXML(URL resource, URL xsd) throws IOException {
		try {
			readInternal(resource, xsd, false);
		} catch (XMLStreamException xml) {
			throw new IOException("XML Error while reading " + resource, xml);
		} catch (SAXException sax) {
			throw new IOException("XML Error while reading " + resource, sax);
		}
	}
	private FXML(URL resource, URL xsd, boolean ignoreIdRefs) throws IOException, XMLStreamException, SAXException {
		try {
			readInternal(resource, xsd, ignoreIdRefs);
		} catch (XMLStreamException xml) {
			throw new IOException("XML Error while reading " + resource, xml);
		} catch (SAXException sax) {
			throw new IOException("XML Error while reading " + resource, sax);
		}
	}


	public FXMLElement getRoot() 
	{
		return _root;
	}
	
	public void loadOverlay(URL resource, URL xsd) throws XMLStreamException, IOException, SAXException
	{
		FXML overlay = new FXML(resource, xsd, true);
	
		FXMLElement xml = overlay.getRoot();
		merge(xml);
	}
	
	private void merge(FXMLElement xml) 
	{
		if (!xml.tag.equals("overlay")) throw new RuntimeException("Overlay should have root element 'overlay'");
		
		for (FXMLElement child : xml.children())
		{
			if (!child.hasAttribute("id")) throw new RuntimeException("Children of 'overlay' must have ID to link");
			String id = child.getAttibute("id");
			
			FXMLElement toMerge = findById(id);
			mergeElements(toMerge, child);
		}
	}
	
	

	private void mergeElements(FXMLElement mergeInto, FXMLElement mergeFrom) 
	{
		// First merge attributes
		for (Map.Entry<String, String> att : mergeFrom.attributes().entrySet())
		{
			mergeInto.setAttribute(att.getKey(), att.getValue());
		}
		
		if (mergeFrom.hasChars()) {
			if (mergeInto.hasChildren()) throw new RuntimeException("Can't merge, target has children, source has char data!");
			mergeInto.setChars(mergeFrom.getChars());
		} else if (mergeFrom.hasChildren()) 
		{
			if (mergeInto.hasChars()) throw new RuntimeException("Can't merge, target has char data, source has children!");
			// Them merge children:
			for (FXMLElement fromChild : mergeFrom.children())
			{
				if (fromChild.hasAttribute("name") && mergeInto.getChildByName(fromChild.getAttibute("name")) != null) 
				{
					mergeElements(mergeInto.getChildByName(fromChild.getAttibute("name")), fromChild);					
				} else {
					mergeInto.addChild(fromChild);
				}
			}
		}
		
		
		
	}

	public FXMLElement findById(String id) 
	{
		if (_root.hasAttribute("id") && _root.getAttibute("id").equals(id)) return _root;
		return findById(id, _root);
	}

	private FXMLElement findById(String id, FXMLElement root)
	{
		for (FXMLElement child : root.children())
		{
			if (child.hasAttribute("id") && child.getAttibute("id").equals(id)) return child;
			if (child.children().size() > 0) {
				FXMLElement found = findById(id, child);
				if (found != null) return found;
			}
		}
			
		return null;
	}
		

	private void readInternal(URL resource, URL xsd, final boolean ignoreIdRefs) throws XMLStreamException, IOException, SAXException
	{
		Validator validator = null;
		
		if (xsd != null) {
			SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			Schema schema = factory.newSchema(xsd);
			validator = schema.newValidator();
			validator.setErrorHandler(new ErrorHandler() {
				
				@Override
				public void warning(SAXParseException exception) throws SAXException {
					throw exception;
				}
				
				@Override
				public void fatalError(SAXParseException exception) throws SAXException {
					throw exception;
				}
				
				@Override
				public void error(SAXParseException exception) throws SAXException {
					if (ignoreIdRefs && exception.getMessage().contains("cvc-id.1")) {
						return;
					}
					throw exception;
				}
			});
			validator.validate(new StreamSource(resource.openStream()));
		}
		
		XMLInputFactory f = XMLInputFactory.newInstance();
		XMLEventReader er = f.createXMLEventReader(resource.openStream());
		LinkedList<FXMLElement> stack = new LinkedList<FXMLElement>();

		FXMLElement cur = null;
		
		while(er.hasNext()) {
			XMLEvent event = er.nextEvent();
			
			switch (event.getEventType())
			{
			case XMLEvent.START_ELEMENT:
			{
				StartElement se = event.asStartElement();
				String name = se.getName().toString();
				
				
				FXMLElement newCur = new FXMLElement(name);
				
				if (cur != null) {
					stack.push(cur);
					cur.addChild(newCur);
				}
				
				cur = newCur;
				
				if (_root == null) _root = cur;
				
				Iterator<?> it = se.getAttributes();
				while (it.hasNext()) 
				{
					Attribute attr = (Attribute)it.next();
					String attName = attr.getName().toString();
					String attValue = attr.getValue();
					cur.setAttribute(attName, attValue);
				}
				
			}
			break;
			case XMLEvent.CDATA:
			case XMLEvent.CHARACTERS:
				Characters chars = event.asCharacters();
				
				String result = chars.getData().trim();
				if (result.length() == 0) break;
				
				cur.setChars(result);
				
			break;
			case XMLEvent.END_ELEMENT:
			{
				if (stack.size() > 0) {
					cur = stack.pop();
				} else {
					cur = null;
				}
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
	}
	
	
	
}
