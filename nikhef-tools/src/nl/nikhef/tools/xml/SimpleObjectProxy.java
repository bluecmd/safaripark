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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import nl.nikhef.tools.Converter;

/**
 * XOLProxy which instantiates a plain object using conventions.
 * 
 * <ul>
 * <li>All attributes should be setters, for example an attribute 'id' should
 * be setId(value)</li>
 * <li>Setting of parent is supported by the 'setParent' setter (Optional)</li>
 * <li>Setting of children is supported by the 'add' method (Optional) or add<Simple-class-name>(instance of class>.</li>
 * <li>Setting of text content is supported by the 'setValue' method</li>
 * </ul>
 * 
 * All contentions marked 'Optional' will cause no warning or error when not found.
 * 
 * The SimpleObjectProxy will attempt to convert the provided value to type of
 * method argument. This is done by using the Converter.
 * 
 * @see nl.nikhef.tools.Converter
 * @see nl.nikhef.tools.Conversion
 * 
 * @author vincentb
 *
 * @param <I>	Object to instantiate.
 */
public class SimpleObjectProxy<I> extends XOLProxyBase<I> 
{
	
	private I _instance;
	private Class<I> _clazz;
	private Method _addAllChildMethod;
	private Map<Class<?>, Method> _addSpecificChild;
	
	private Map<String, Method> _props = new HashMap<String, Method>();
	
	@SuppressWarnings("unchecked")
	public SimpleObjectProxy(I inst) 
	{
		_instance = inst;
		_clazz   = (Class<I>) inst.getClass();
		findMethods();
	}
	
	public SimpleObjectProxy(Class<I> clazz)
	{
		try {
			_clazz = clazz;
			_instance = clazz.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException("Can't instantiate '" + clazz.getSimpleName() + "'", e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Can't instantiate '" + clazz.getSimpleName() + "'", e);
		}
		findMethods();
	}
	
	
	
	private void findMethods() {
		try {
			Method[] methods = _clazz.getMethods();
			
			for (Method method : methods) {
				
				String name = method.getName();
				int parCount = method.getParameterTypes().length;
			
				if (parCount == 1) 
				{
					if (name.equals("add")) { 
						_addAllChildMethod = method;
					} else if (name.startsWith("add")) {
						Class<?> paramType = method.getParameterTypes()[0];
						String paramName = paramType.getSimpleName();
						if (name.substring(3).equals(paramName)) {
							if (_addSpecificChild == null) {
								_addSpecificChild = new HashMap<Class<?>, Method>();
							}
							_addSpecificChild.put(paramType, method);
						}
					} else if (name.startsWith("set")) 
					{
					String attName = name.substring(3, 4).toLowerCase() + name.substring(4, name.length());
					_props.put(attName, method);
					}
				}
			}
			
		} catch (SecurityException e) {
			throw new RuntimeException("Can't introspect '" + _clazz.getSimpleName() + "'", e);
		}
	}

	@Override
	public boolean setAttribute(String name, String obj) 
	{
		name = XOL.beanifyProperty(name);
		
		try {
			if (!_props.containsKey(name)) {
				return false;
			}
			
			Method m = _props.get(name);
			
			Class<?> pType =m.getParameterTypes()[0];
			
			Object convObj = Converter.convert(obj, pType);
			
			m.invoke(_instance, convObj);
			return true;
		} catch (Exception e) {
			throw new RuntimeException("Attribute '" + name + "' can not be assigned", e);
		}
	}
	
	@Override
	public void setParent(XOLProxy<?> obj)
	{
		if (!_props.containsKey("parent")) return;
		Method m = _props.get("parent");
		try {
			
			Class<?> pType = m.getParameterTypes()[0];
			
			
			if (!pType.isAssignableFrom(obj.getInstance().getClass())) return;
			
			m.invoke(_instance, obj.getInstance());
		} catch (Exception e) {
			throw new RuntimeException("Parent can not be assigned", e);
		}
	}

	
	@Override
	public void addChild(XOLProxy<?> obj)
	{
		if (_addAllChildMethod == null)
			return;
	
		Object val = obj.getInstance();
		if (val == null) return;
		
		Method tmp = null;

		try {

			if (_addSpecificChild != null) {
				for ( Map.Entry<Class<?>, Method> e : _addSpecificChild.entrySet()) {
					Class<?> mClass = e.getKey();
					Class<?> vClass = val.getClass();
					if (mClass.isAssignableFrom(vClass)) {
						tmp = e.getValue();
						e.getValue().invoke(_instance,val);
						return;
					}
				}
			}
		
			tmp =_addAllChildMethod;
			_addAllChildMethod.invoke(_instance, val);
		} catch (Exception e) {
			throw new RuntimeException(String.format("Can not add child to class '%s', using method %s", _clazz.getSimpleName(), tmp), e);
		}
	}

	@Override
	public I getInstance() {
		return _instance;
	}

	@Override
	public boolean setContent(String data) {
		if (!_props.containsKey("value")) return false;
		Method m = _props.get("value");
		try {
			m.invoke(_instance, data);
			return true;
		} catch (Exception e) {
			throw new RuntimeException("CharacterData can not be assigned", e);
		}
	}

	@Override
	public String getTypeName() {
		return "Class '" + _clazz.getSimpleName() + "'"; 
	}

	@Override
	public void complete() {
	}

}
