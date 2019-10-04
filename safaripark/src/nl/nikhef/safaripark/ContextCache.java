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
package nl.nikhef.safaripark;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import nl.nikhef.sfp.SFPDevice;
import nl.nikhef.sfp.SFPDeviceListener;
import nl.nikhef.sfp.SFPManager;
import nl.nikhef.sfp.SFPProvider;
import nl.nikhef.sfp.SFPProviderListener;
import nl.nikhef.sfp.ddmi.DDMI;
import nl.nikhef.sfp.ddmi.DDMIContext;
import nl.nikhef.sfp.ddmi.DDMILoader;

public class ContextCache implements SFPProviderListener {

	
	private class  ContextCacheEntry implements SFPDeviceListener {
		
		private Map<Integer, DDMIContext> _ctx = new HashMap<Integer, DDMIContext>();
		private SFPDevice _dev;
		
		public ContextCacheEntry(SFPDevice dev) 
		{
			_dev = dev;
			_dev.addDeviceListener(this);
			
		}
		

		public void finish() {
			_dev.removeDeviceListener(this);
		}

		@Override
		public void sfpModuleStateChanged(SFPDevice dev, int bay) 
		{
			// called from outside of context, so we need to synchronize
			synchronized (ContextCache.this) 
			{
				if (!dev.isModulePresent(bay) && _ctx.containsKey(bay)) 
				{
					_ctx.remove(bay);
				}
			}
		}


		public DDMIContext makeOrGetContext(int bay) throws IOException 
		{
			if (!_ctx.containsKey(bay)) _ctx.put(bay, new DDMIContext(_dev.getLink(bay), _loader.getDDMI()));
			
			return _ctx.get(bay);
		}
		
	}
	
	private Map<SFPDevice, ContextCacheEntry> _cache = new HashMap<SFPDevice, ContextCacheEntry>();
	
	private DDMILoader _loader;
	
	public ContextCache(SFPManager sfpMan, DDMILoader loader) 
	{
		sfpMan.addSFPProviderListener(this);
		_loader = loader;
	}
	
	
	private synchronized DDMIContext makeOrGetEntry(SFPDevice dev, int bay) throws IOException
	{
		if (!_cache.containsKey(dev)) _cache.put(dev, new ContextCacheEntry(dev));
		
		
		return _cache.get(dev).makeOrGetContext(bay);
	}
	
	private synchronized void removeEntry(SFPDevice dev) 
	{
		if (!_cache.containsKey(dev)) return;
		_cache.get(dev).finish();
		_cache.remove(dev);
	}
	
	
	public DDMIContext getContextFor(SFPDevice dev, int bay) throws IOException
	{
		return makeOrGetEntry(dev, bay);								
	}


	@Override
	public void sfpDeviceAdded(SFPProvider provider, SFPDevice dev) {
	}


	@Override
	public void sfpDeviceRemoved(SFPProvider provider, SFPDevice dev) {
		removeEntry(dev);
	}
	
}
