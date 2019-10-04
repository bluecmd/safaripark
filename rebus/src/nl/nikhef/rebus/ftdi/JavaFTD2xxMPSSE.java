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

import com.ftdi.BitModes;
import com.ftdi.FTD2XXException;
import com.ftdi.FTDevice;

/**
 * MPSSE instance.
 * 
 * @author vincentb
 */
public class JavaFTD2xxMPSSE extends MPSSE {
	
	public FTDevice ftdi;
	private int _pinDirs;

	/**
	 * Creates an MPSSE instance.
	 * 
	 * @param dev		The FTDevice (must be open).
	 * @throws FTD2XXException 
	 * 
	 */
	public JavaFTD2xxMPSSE(FTDevice dev) throws IOException
	{
		// System.out.println("JavaFTD2xxMPSEE created");
		ftdi = dev;
		ftdi.open();
	}
	

	public void switchMode(int pinDirs) throws IOException {
		_pinDirs = pinDirs;
		ftdi.setBitMode((byte)pinDirs, BitModes.BITMODE_MPSSE);
		// ftdi.setTimeouts(500, 500);
		ftdi.setLatencyTimer((short)3);
		
		// clear any crap in the buffer
		ftdi.purgeBuffer(true, true);
		syncMPSSE();
	}

	public int getReceiveQueue() throws IOException
	{
		return (int)ftdi.getQueueStatus();
	}
	
	
	protected void checkQueue() throws IOException {
		// System.out.printf("checkQueue -> ");
		int q = 0;
		try {
			q = ftdi.getQueueStatus();
		} catch (FTD2XXException e) {
			// System.out.printf("Error, with FTDI handle, attempting to recover\n");
			ftdi.open();
			switchMode(_pinDirs);
		}
		if (q == 0) {
			// System.out.println("Ok");	
			return;
		}
		
		StringBuilder sb = new StringBuilder("MPSSE Unexpected data:");

		while (q > 0) 
		{
			int b = ftdi.read();
			if (b == 0xFA) {
				if (q == 1) {
					sb.append(" BadCommand=TRUNC!!");
				} else {
					b = ftdi.read();
					sb.append(String.format(" BadCommand=%02x", b));
				}
			} else {
				sb.append(String.format(" ???=%02x", b));
			}
			q = ftdi.getQueueStatus();
		}

		ftdi.purgeBuffer(true, true);
		// System.out.println("Error:" + sb.toString());
		throw new IOException(sb.toString());
	}

	
	
	public void close() throws FTD2XXException {
		// System.out.println("FTDI Close");
		ftdi.close();
	}


	@Override
	public void read(byte[] buf) throws IOException {
		ftdi.read(buf);
	}


	@Override
	public void write(byte[] buf) throws IOException {
		ftdi.write(buf);
	}


}
