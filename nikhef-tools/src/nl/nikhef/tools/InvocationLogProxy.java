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
package nl.nikhef.tools;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

public class InvocationLogProxy  implements InvocationHandler {


	private final Object _delegate;
	private final String _className;

	private InvocationLogProxy(final Object delegate) {
		_delegate = delegate;
		_className = delegate.getClass().getSimpleName();
	}
		  
	public static final <T> T wrap(T object, Class<T> iface)
	{
		return iface.cast( Proxy.newProxyInstance(iface.getClassLoader(), new Class[] { iface }, new InvocationLogProxy(object)) );
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		System.out.printf("%9d %s.%s(%s)\n", System.currentTimeMillis(), _className, method.getName(), Arrays.toString(args));
		try
		{
			final Object ret = method.invoke(_delegate, args);
			System.out.printf("%9d -> return: %s\n", System.currentTimeMillis(), ret);
			return ret;
		} catch (InvocationTargetException t) {
			System.out.printf("%9d -> thrown: %s\n", System.currentTimeMillis(), t.getTargetException());
			throw t;
		}
	}

}
