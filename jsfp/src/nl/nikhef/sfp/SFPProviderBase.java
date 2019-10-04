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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import nl.nikhef.tools.ListenerManager;

public abstract class SFPProviderBase implements SFPProvider {

	private ListenerManager<SFPProviderListener> _splMgr = 
			new ListenerManager<SFPProviderListener>(SFPProviderListener.class);
	private List<SFPDevice> _devices = new ArrayList<SFPDevice>();
	private Set<SFPDevice> _updSet = new HashSet<SFPDevice>();
	private List<SFPDevice> _umDevices = Collections.unmodifiableList(_devices);
	
	@Override
	public void addSFPDeviceListener(SFPProviderListener l) 
	{
		_splMgr.addListener(l);
	}

	@Override
	public void removeSFPDeviceListener(SFPProviderListener l) {
		_splMgr.removeListener(l);
	}
	
	protected void updAdd(SFPDevice dev) {
		_updSet.add(dev);
	}
	
	protected void updProcess() 
	{
		for (SFPDevice dev : _updSet) 
		{
			if (!_devices.contains(dev)) addDevice(dev);
		}
		
		Iterator<SFPDevice> it = _devices.iterator(); 
		
		while (it.hasNext()) {
			SFPDevice dev = it.next();
			if (!_updSet.contains(dev)) {
				it.remove();
				dev.shutdown();
				_splMgr.getProxy().sfpDeviceRemoved(this, dev);
			}
		}
		_updSet.clear();
	}
	
	protected void addDevice(SFPDevice dev)
	{
		
		_devices.add(dev);
		_splMgr.getProxy().sfpDeviceAdded(this, dev);

	}

	protected void removeDevice(SFPDevice dev)
	{
		_devices.remove(dev);
		dev.shutdown();
		_splMgr.getProxy().sfpDeviceRemoved(this, dev);
	}
	

	@Override
	public List<SFPDevice> getDevices() {
		return _umDevices;
	}
	
	@Override
	public String toString() {
		return getName();
	}

	@Override
	public void updateDevices() {
		for (SFPDevice dev : getDevices()) {
			dev.updateModules();
		}
	}
	
}
