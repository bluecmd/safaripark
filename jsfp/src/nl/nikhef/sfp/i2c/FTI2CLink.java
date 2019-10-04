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
package nl.nikhef.sfp.i2c;

import java.io.IOException;

import nl.nikhef.rebus.ftdi.MPSSE_I2C;
import nl.nikhef.rebus.ftdi.MPSSE;

public class FTI2CLink implements I2CLink {


	private MPSSE _mpsse;
	private MPSSE_I2C   _i2c;
	
	public FTI2CLink(MPSSE mpsse) throws IOException {
		_mpsse = mpsse;
		_i2c   = new MPSSE_I2C(mpsse, MPSSE_I2C.SPEED_NORMAL, true);
		_i2c.wakeI2C();
	}
	
	
	@Override
	public void open() throws IOException
	{
		
	}

	@Override
	public void close() throws IOException 
	{
		
	}

	@Override
	public void i2cWrite(int addr, byte[] data) throws IOException {
		_i2c.write(addr, data);
	}

	@Override
	public byte[] i2cRead(int addr, int len) throws IOException {
		return _i2c.read(addr, len);
	}

	@Override
	public byte[] i2cWrRd(int addr, byte[] dataWr, boolean contRd, int rdLen)
			throws IOException {
		
		if (contRd) {
			i2cWrite(addr, dataWr);
			return i2cRead(addr, rdLen);
		} else {
			return _i2c.writeRead(addr, dataWr, rdLen);
		}
	}

	@Override
	public void shutdown() throws IOException {
		_mpsse.close();
	}

}
