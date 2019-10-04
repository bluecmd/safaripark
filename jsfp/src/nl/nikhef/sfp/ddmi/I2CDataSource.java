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
package nl.nikhef.sfp.ddmi;

import java.io.IOException;

import nl.nikhef.sfp.i2c.I2CLink;

public class I2CDataSource extends DataSource {

	private final int _addr;
	
	public I2CDataSource(int addr, int start, int end) {
		super(start, end);
		this._addr = addr;
	}
	
	@Override
	byte[] readMedia(DDMIContext ctx, int off, int len) {
		
		
		I2CLink i2c = ctx.getI2CLink();
		
		synchronized (i2c)
		{
		
		if (i2c == null) return new byte[len];
	
		// System.out.printf("Read media %02x, off=%d, len=%d\n", _addr, off, len);
		
		byte[] offData = new byte[] { (byte)off };
		try {
			i2c.open();
			return i2c.i2cWrRd(_addr, offData, true, len);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				i2c.close();
			} catch (IOException e) {
			}
		}
		}
	}

	@Override
	void writeMedia(DDMIContext ctx, int off, byte[] data) {
		
		I2CLink i2c = ctx.getI2CLink();
		
		if (i2c == null) return;
		
		byte[] out = new byte[data.length + 1];
		out[0] = (byte)off;
		for (int i = 0; i < data.length; i++) {
			out[i + 1] = data[i]; 
		}
		try {
			i2c.open();
			i2c.i2cWrite(_addr, out);
		} catch (IOException e) {
			throw new RuntimeException("Failed to write at offset="+ off + ", " + data.length + " byte(s)", e);
		} finally {
			try {
				i2c.close();
			} catch (IOException e) {
			}
		}
	}

	
	@Override
	public String toString() {
		return String.format("I2CDataSource(addr=%02x)", _addr);
	}
	
	@Override
	public String getPath() {
		return Integer.toHexString(_addr);
	}
}
