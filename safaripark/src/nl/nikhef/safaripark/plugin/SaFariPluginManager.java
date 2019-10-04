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
package nl.nikhef.safaripark.plugin;

import java.util.ServiceLoader;

public class SaFariPluginManager {

	public SaFariPluginManager() {
	}
	
	public void loadPlugins() 
	{
		
		SaFariPluginContext ctx = new SaFariPluginContext();
		
		ServiceLoader<SaFariPlugin> plugs = ServiceLoader.load(SaFariPlugin.class);
		for (SaFariPlugin sp : plugs) {
			
			sp.saFariPluginInit(ctx);
		}
	}

}
