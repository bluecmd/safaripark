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

/**
 * A XOLProxy adds a flexible layer of indirectness to object creation and content filling. 
 * It allows deviation from the 'entity => object' scheme by implementing a custom proxy. 
 *
 * A XOLProxy are instantiated by a XOLProxyFactory. 
 * 
 * Normally the SimpleObjectProxy is used. 
 * 
 * @see SimpleObjectProxy
 * @see XOLProxyFactory
 * 
 * @author vincentb
 *
 * @param <I>	The type this proxy instantiates. May also be Void
 *              if it does not directy instantiate anything.
 */
public interface XOLProxy<I> {
	
	
	/**
	 * Sets the XOL instance which is rendering the XML file. Can be used to retrieve ID-ed references.
	 * 
	 * @param xol		The XOL instance.
	 */
	public void setXOL(XOL xol);

	/**
	 * Set an attribute on the proxyed object.
	 * 
	 * @param name		The name of the attribute
	 * @param val		The value of the attribute
	 * @return			true - if succesful, false - if not.
	 */
	public boolean setAttribute(String name, String val);
	
	/**
	 * Adds a child proxy object. Invoked if child object is complete. 
	 */
	public void addChild(XOLProxy<?> obj);
	
	/**
	 * Set parent object, invoked before setting attributes.
	 * 
	 * @note  Parent does not have knowledge of child at this point
	 * 
	 * @param obj		Parent object.
	 */
	public void setParent(XOLProxy<?> obj);
	
	/**
	 * Returns the instance of the object.
	 * 
	 * @return		The instance, or null if this object does not directly instantiates.
	 */
	public I getInstance();
	
	/**
	 * Returns a humam-readable type name for exceptions and tracing.
	 * 
	 * @return	Human readable type name for this proxy class.
	 */
	public String getTypeName();
	
	
	/**
	 * Invoked when the object is completed.
	 */
	public void complete();

	/**
	 * Sets textual content on this object.
	 * 
	 * @param data		The content to set
	 * 
	 * @return			true  - if successful.
	 *                  false - if the proxy does not support setting of content.   
	 */
	public boolean setContent(String data);
	
}
