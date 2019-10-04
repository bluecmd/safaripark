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

import javax.xml.stream.XMLStreamException;

import com.ftdi.FTDevice;

import nl.nikhef.rebus.dev.PCA9848;
import nl.nikhef.rebus.ftdi.MPSSE_I2C;
import nl.nikhef.rebus.ftdi.JavaFTD2xxMPSSE;
import nl.nikhef.rebus.ftdi.MPSSE;
import nl.nikhef.sfp.ddmi.DDMI;
import nl.nikhef.sfp.ddmi.DDMIContext;
import nl.nikhef.sfp.ddmi.DDMILoader;
import nl.nikhef.sfp.i2c.I2CLink;
import nl.nikhef.sfp.i2c.SimI2CLink;
import nl.nikhef.tools.Utils;

public class Test2 {

	public static void main(String[] args) throws XMLStreamException, IOException {
		
		
		
		MPSSE mpsse = new JavaFTD2xxMPSSE(FTDevice.getDevicesBySerialNumber("NK1BYBILA").get(0));
		MPSSE_I2C i2c = new MPSSE_I2C(mpsse, 100000, false);

		
		PCA9848 mux = new PCA9848(i2c, PCA9848.ADDRESS_HHH);
		mux.setSingle(0);
		i2c.write(0x50, new byte[] { 0 });
		Utils.dumpHex("EEPROM", i2c.read(0x50, 128));
		i2c.write(0x51, new byte[] { 127, 2 });
		i2c.write(0x51, new byte[] { (byte)132 });
		Utils.dumpHex("Page 2", i2c.read(0x51, 2));
		mpsse.close();
		//DDMIContext ctx = new DDMIContext(link, ddmi);

		
		
		
		//ddmi.prettyPrint(ctx);

	}

}
