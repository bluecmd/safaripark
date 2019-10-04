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
package nl.nikhef.rebus.dev;

import java.io.IOException;

import nl.nikhef.rebus.ftdi.MPSSE_I2C;

public class PCA9848 {

	public static final int ADDRESS_BASE = 0x70;
	public static final int ADDRESS_LLL = 0x70;
	public static final int ADDRESS_LLH = 0x71;
	public static final int ADDRESS_LHL = 0x72;
	public static final int ADDRESS_LHH = 0x73;
	public static final int ADDRESS_HLL = 0x74;
	public static final int ADDRESS_HLH = 0x75;
	public static final int ADDRESS_HHL = 0x76;
	public static final int ADDRESS_HHH = 0x77;
	
	/*
	private static final int MASK_CHANNEL_0	= 0x01;
	private static final int MASK_CHANNEL_1	= 0x02;
	private static final int MASK_CHANNEL_2	= 0x04;
	private static final int MASK_CHANNEL_3	= 0x08;
	private static final int MASK_CHANNEL_4	= 0x10;
	private static final int MASK_CHANNEL_5	= 0x20;
	private static final int MASK_CHANNEL_6 = 0x40;
	private static final int MASK_CHANNEL_7 = 0x80;
	*/
	private MPSSE_I2C _i2c;
	private int _addr;
	
	public PCA9848(MPSSE_I2C i2c, int addr)
	{
		_i2c = i2c;
		_addr = addr;
	}

	/**
	 * Enable one or more channels.
	 * 
	 * @param bitmask		the mask
	 * @throws IOException		On error
	 */
	public void setChannels(int bitmask) throws IOException 
	{
		// System.out.printf("Select: %02x\n", bitmask);
		
		_i2c.write(_addr, new byte[] { (byte)bitmask });		
	}
	

	/**
	 * Set single channel.
	 * 
	 * @param chNo		Channel number (0..7), or -1 to disable all.
	 * 
	 * @throws IOException		On error 
	 */
	public void setSingle(int chNo) throws IOException {
		if (chNo == -1) {
			setChannels(0);
		} else if (chNo >=0 && chNo <= 7) {
			setChannels(1 << chNo);
		} else {
			throw new IOException(String.format("PCA9848 Channel %d does not exist", chNo));
		}
	}

	public int getChannels() throws IOException {
		
		byte[] b = _i2c.read(_addr, 1);
		return b[0];
	}
	
}
