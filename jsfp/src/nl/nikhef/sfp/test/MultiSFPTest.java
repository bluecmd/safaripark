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
package nl.nikhef.sfp.test;

import java.io.IOException;

import nl.nikhef.sfp.SFPDevice;
import nl.nikhef.sfp.SFPDeviceListener;
import nl.nikhef.sfp.SFPManager;
import nl.nikhef.sfp.SFPProvider;
import nl.nikhef.sfp.SFPProviderListener;
import nl.nikhef.sfp.i2c.I2CLink;
import nl.nikhef.tools.Utils;

public class MultiSFPTest {

	private static void writeVendor(SFPDevice dev, int i) throws IOException {
		I2CLink link = dev.getLink(i);
		link.open();
		
		byte[] writeAccess = new byte[] { 123, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF };
		
		link.i2cWrite(0x51, writeAccess);
		
		// get access
		// vendor starts @ 20
		byte[] addr = { 0 };
		byte[] data = link.i2cWrRd(0x50, addr, true, 256);
		
		Utils.dumpHex("EEPROM:", data);
		
		
		//Arrays.fill(data,(byte)0xFF);
		//link.i2cWrite(0x50, Utils.arrayConcat(addr, data));
		
		/*System.out.println("Vendor first: " + new String(data, 4, 16).trim());
		
		Arrays.fill(data, 4, 20, (byte)' ');
		
		byte[] newName = "Test".getBytes();
		
		System.arraycopy(newName, 0, data, 4, newName.length);
		
		
		
		
		
		try {
			Thread.sleep(10);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		byte[] vendor = link.i2cWrRd(0x50, new byte[] { 20 }, true, 16);
		System.out.println("Vendor next: " + new String(vendor).trim());

		*/
		link.close();
	}
	
	public static void main(String[] args) throws InterruptedException 
	{
		SFPManager sfpMgr = new SFPManager();
		
		sfpMgr.addSFPProviderListener(new SFPProviderListener() 
		{
			
			@Override
			public void sfpDeviceRemoved(SFPProvider provider, SFPDevice dev) {
				
			}
			
			@Override
			public void sfpDeviceAdded(SFPProvider provider, SFPDevice dev) {
				System.out.printf("Device: %s/%s, Bays = %d\n", dev.getClass().getSimpleName(), dev.getSerial(), dev.getBayCount());
				
				
				dev.addDeviceListener(new SFPDeviceListener() {
					
					@Override
					public void sfpModuleStateChanged(SFPDevice dev, int bay) {
						if (dev.isModulePresent(bay)) {
							System.out.printf("Module added to bay %d: %s\n", bay, dev.getModuleName(bay));
							
							if (bay == 1) {
								try {
									writeVendor(dev, 1);
								} catch (IOException e) {
									e.printStackTrace();
								}								
							}
							
						} else {
							System.out.printf("Module removed from bay %d\n", bay);	
						}
						
					}

					
				});
			}
		});
		
		
		sfpMgr.update(true);
		Thread.sleep(1000);
	}

}
