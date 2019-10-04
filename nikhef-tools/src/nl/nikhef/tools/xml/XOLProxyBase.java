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
 * Abstract base class for XOLProxy instances.
 * 
 * @author vincentb
 *
 * @param <T>	The type which is instantiated.
 */
public abstract class XOLProxyBase<T> implements XOLProxy<T> 
{

	
	private XOLProxy<?> _parent;
	private XOL _xol;
	
	
	@Override
	public boolean setAttribute(String name, String obj) {
		return false;
	}
	
	@Override
	public void setXOL(XOL xol) {
		if (_xol != null) throw new RuntimeException("XOL was already assigned!");
		_xol = xol;
	}
	
	public XOL getXol() {
		return _xol;
	}

	@Override
	public void addChild(XOLProxy<?> obj) {
	}

	@Override
	public T getInstance() {
		return null;
	}

	@Override
	public void setParent(XOLProxy<?> obj) {
		_parent = obj;
	}
	
	public <P extends XOLProxy<?>> P getParentAs(Class<P> clazz) {
		return clazz.cast(_parent);
	}

	@Override
	public boolean setContent(String data) {
		return false;
	}	
	
	@Override
	public void complete() {
	}
}
