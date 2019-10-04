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
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ListenerManager<T> 
{

	public interface EventDispatcher<T>
	{
		public void dispatch(Collection<T> c, Method m, Object[] params) throws Exception;
	}
	
	public static class DefaultDispatcher<T> implements EventDispatcher<T>
	{

		@Override
		public void dispatch(Collection<T> c, Method m, Object[] params) throws Exception {
			for (T obj : c) {
				try {
					m.invoke(obj, params);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	private Set<T> _listeners = Collections.emptySet();
	private Collection<T> _uListeners = Collections.emptySet();
	private Class<T> _clazz;
	private  EventDispatcher<T> _dispatcher;
	
	private T _proxy;
	
	private InvocationHandler _ih = new InvocationHandler() 
	{
		
		@Override
		public Object invoke(Object self, Method m, Object[] params) throws Throwable 
		{
			try {
				_dispatcher.dispatch(_listeners, m, params);
			} catch (Exception ex) 
			{
				ex.printStackTrace();				
			}

			return null;
		}
	}; 
	
	public ListenerManager(Class<T> clazz)
	{
		this(clazz, new DefaultDispatcher<T>());
	}
	
	
	public ListenerManager(Class<T> clazz, EventDispatcher<T> disp)
	{
		_clazz = clazz;
		_dispatcher = disp;
		
		_proxy = _clazz.cast(Proxy.newProxyInstance(clazz.getClassLoader(), 
				new Class[] { clazz }, _ih));
	}
	
	// Collections.unmodifiableCollection(_listeners);
	public synchronized void addListener(T listener)
	{
		Set<T> s = new HashSet<T>();
		s.addAll(_listeners);
		s.add(listener);		
		_listeners = s;
		_uListeners = Collections.unmodifiableCollection(_listeners);
	}
	
	public synchronized void removeListener(T listener)
	{
		Set<T> s = new HashSet<T>();
		s.addAll(_listeners);
		s.remove(listener);
		_listeners = s;
		_uListeners = Collections.unmodifiableCollection(_listeners);
	}
	
	public Collection<T> getListeners() {
		return _uListeners;
	}
	
	public T getProxy() {
		return _proxy;
	}
}
