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

import java.lang.reflect.Method;
import java.util.Collection;

import javax.swing.SwingUtilities;

public class AWTDispatcher<T> implements ListenerManager.EventDispatcher<T> {

	private static class DispatchTask<T> implements Runnable
	{

		public final Collection<T> col;
		public final Method m;
		public final Object[] params;
		
		public DispatchTask(Collection<T> c, Method m, Object[] params) 
		{
			this.col = c;
			this.m = m;
			this.params = params;
		}

		@Override
		public void run() {
			for (T obj : col) {
				try {
					m.invoke(obj, params);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	@Override
	public void dispatch(Collection<T> c, Method m, Object[] params) throws Exception {
		DispatchTask<T> dp = new DispatchTask<T>(c, m, params);
		SwingUtilities.invokeLater(dp);
	}
	


}
