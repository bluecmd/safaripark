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
package nl.nikhef.sfp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Logger;

/**
 * SFPManager keeps a list of SFPProviders.
 * 
 * @author vincentb
 *
 */
public class SFPManager {

	private static final Logger LOG = Logger.getLogger(SFPManager.class.getSimpleName());
	
	
	private List<SFPProvider> _providers = new ArrayList<SFPProvider>();
	private List<SFPProvider> _umProviders = Collections.unmodifiableList(_providers);
	
	
	public SFPManager() 
	{
		ServiceLoader<SFPProvider> prov = ServiceLoader.load(SFPProvider.class);
		

		for (SFPProvider sp : prov)
		{
			LOG.info(String.format("Found provider: %s", sp.getName()));
			_providers.add(sp);
		}
		
	}
	
	
	public List<SFPProvider> getProviders()
	{
		return _umProviders;
	}
	
	/**
	 * Proxy function which will add the listener to each provider.
	 */
	public void addSFPProviderListener(SFPProviderListener l)
	{
		for (SFPProvider prov : _providers)
		{
			prov.addSFPDeviceListener(l);
		}
	}
	
	/**
	 * Proxy function which will remove the listener from each provider.
	 */
	public void removeSFPProviderListener(SFPProviderListener l)
	{
		for (SFPProvider prov : _providers){
			prov.removeSFPDeviceListener(l);
		}
	}
	
	
	/**
	 * Must be invoked each second to update the sfp device state.
	 * 
	 * @param scanForNewDevices		Whether or not to scan for new devices on the bus.
	 */
	public synchronized void update(boolean scanForNewDevices)
	{
		for (SFPProvider prov : _providers)
		{
			if (scanForNewDevices) prov.scanForNewDevices();
			else prov.updateDevices();
		}
	}
			
	
	

	
	
}
