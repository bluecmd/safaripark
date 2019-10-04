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
 * XOLProxyFactory which creates SimpleObjectProxy instances. 
 * 
 * @author vincentb
 *
 * @param <I>
 */
public class SimpleProxyFactory<I> implements XOLProxyFactory<I> {

	private Class<? extends XOLProxy<I>> _class;

	public SimpleProxyFactory(Class<? extends XOLProxy<I>> clazz) 
	{
		_class = clazz;
	}
	
	@Override
	public XOLProxy<? extends I> newProxy() {
		try {
			return _class.newInstance();
		} catch (Exception e) {
			throw new RuntimeException("Failed to instantiate XOLProxy", e);
		}
	}

}
