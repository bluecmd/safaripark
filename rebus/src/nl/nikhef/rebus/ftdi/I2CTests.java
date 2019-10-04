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
package nl.nikhef.rebus.ftdi;



import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.ftdi.FTDevice;

import nl.nikhef.rebus.dev.PCA9848;

public class I2CTests {

	public static FTDevice findFTDevice() throws IOException {
		return FTDevice.getDevices().get(0);
	}
	
	public static void main(String[] args) throws IOException {
		
		MPSSE mpsse = new JavaFTD2xxMPSSE(findFTDevice());
		try {
			Logger.getLogger(FTDevice.class.getName()).setLevel(Level.OFF);
			
			// MPSSE mpsse = new Ftd2xxjMPSSE(findDevice());
			
			
			MPSSE_I2C i2c = new MPSSE_I2C(mpsse, MPSSE_I2C.SPEED_SLOW, false);
			
			
			// set mux to 0
			PCA9848 mux = new PCA9848(i2c, PCA9848.ADDRESS_HHH);
			mux.setSingle(0);
			
		
			// going to try to retrieve the VENDOR NAME
			i2c.write(0x50, new byte[] { 20 });
			byte[] b = i2c.read(0x50, 2);
			
			System.out.printf("Vendor: %s\n", new String(b));
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			mpsse.close();
		}
		
	}
}
