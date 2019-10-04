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

import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

import nl.nikhef.rebus.ftdi.FTDIDevice;
import nl.nikhef.rebus.ftdi.FTDIEnumerator;
import nl.nikhef.rebus.ftdi.FTDIType;

public class MultiSFPProvider extends SFPProviderBase
{
	
	private static final Logger LOG = Logger.getLogger(MultiSFPProvider.class.getSimpleName());
	
	@Override
	public String getName() {
		return "MultiSFP";
	}

	public SFPDevice getDeviceBySerial(String serial) {
		for (SFPDevice dev : getDevices()) {
			if (dev.getSerial().equals(serial)) return dev;
		}
		return null;
	}
	

	public void scanForNewDevices() {
		try {
			List<FTDIDevice> devs;
			synchronized(this)
			{
				
				devs = FTDIEnumerator.getDefault().getDevices();
			}
			
			for (FTDIDevice devRaw : devs)
			{
				// Note: On Linux the MultiSFP A is descriptor is not generated once opened.
				//       But B remains. So we sync on that.
				if (devRaw.getType() == FTDIType.FT_2232H && devRaw.getDescription().equals("MultiSFP B")) {
					
					// 
					
					// Only get the secondary 'B' device, we know the first being A
					String serial = devRaw.getSerial();
					
					// chop of the last part from the serial:
					serial = serial.substring(0, serial.length() - 1);
					
					SFPDevice dev = getDeviceBySerial(serial);
					try {
						
						
						if (dev == null) 
						{
							dev = new MultiSFPDevice(serial, this);
						}
						updAdd(dev);
					} catch (Exception e) {
						LOG.warning("Could not open device: " + e.getMessage());
						// Note: Sometimes on windows, MultiSFP B may be loaded, but MultiSFP A could still be installing.
						//       Both drivers are needed. Unfortunately we can not check for A, since the Linux 'Open' bug
						//       prevents us from doing that (see note above), unless we're going to add all kind of exceptions.
						//       So, we'll just catch the error here, and hope it will work next time.
					}
				}
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		updProcess();		
	}
	
	

}
