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

public abstract class MPSSE {

	public static final int FT_FULLSPEED_CLK = 12000000;
	public static final int FT_HIGHSPEED_CLK = 60000000;
	
	public static final int CMD_BYTE_OUT_P = 0x11;
	public static final int CMD_BIT_OUT_P = 0x13;
	public static final int CMD_BYTE_IN_P = 0x20;
	public static final int CMD_BIT_IN_P = 0x22;

	public static final int CMD_SET_DATA_LOW_BYTE = 0x80;
	public static final int CMD_READ_DATA_LOW_BYTE = 0x81;
	public static final int CMD_SET_DATA_HIGH_BYTE = 0x82;
	public static final int CMD_READ_DATA_HIGH_BYTE = 0x83;
	public static final int CMD_ENA_LOOPBACK = 0x84;
	public static final int CMD_DIS_LOOPBACK = 0x85;
	public static final int CMD_SET_DRIVE0ONLY = 0x9E;
	public static final int CMD_SET_DIV = 0x86;
	public static final int CMD_SEND_IMMEDIATE = 0x87;

	public static final int CMD_DIS_DIV5 = 0x8A;
	public static final int CMD_ENA_DIV5 = 0x8B;
	public static final int CMD_ENA_CLK3PHASE = 0x8C;
	public static final int CMD_DIS_CLK3PHASE = 0x8D;
	public static final int CMD_DIS_ADAPT_CLK = 0x97;
	public static final int CMD_ENA_ADAPT_CLK = 0x96;
	
	public static final int DSH_MVE_CLK_WRITE = 0x01;
	public static final int DSH_MVE_BIT_MODE = 0x02;
	public static final int DSH_MVE_CLK_READ = 0x04;
	public static final int DSH_LSB_FIRST = 0x08;
	public static final int DSH_WRITE_TDI = 0x10;
	public static final int DSH_READ_TDO = 0x20;
	public static final int DSH_WRITE_TMS = 0x40;

	/**
	 * Sets the FTDI device in MPSSE mode.
	 * @param pinDirs
	 * @throws IOException 
	 */
	public abstract void switchMode(int pinDirs) throws IOException;
	
	protected abstract void checkQueue() throws IOException;
	
	protected abstract int getReceiveQueue() throws IOException;
	
	protected abstract void read(byte[] buf) throws IOException;
	
	protected abstract void write(byte[] buf) throws IOException;
	
	public abstract void close() throws IOException;

	private boolean syncBadByte(byte b) throws IOException
	{

		int readToCounter = 0;
		
		write(new byte[] { b });
		
		int rxSize = getReceiveQueue(); 
		while (rxSize < 2 && readToCounter < 10) {
			readToCounter ++;
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}													// short delay
			rxSize = getReceiveQueue();
		}
		
		if (readToCounter < 10) 
		{
			byte[] input = new byte[getReceiveQueue()];
			read(input);
			for (int i = 0; i < input.length - 1; i++)							
			{
				if ((input[i] == (byte)0xFA) && (input[i+1] == b))
				{
					return true;
				}
			}
		}
		
		return false;
	}
	
	private boolean syncMPSSESingle() throws IOException 
	{
		if (!syncBadByte((byte)0xAA)) return false;
		if (!syncBadByte((byte)0xAB)) return false;
		return true;
	}
	
	/**
	 * Should be invoked by subclasses, after initialization.
	 */
	protected void syncMPSSE() throws IOException 
	{
		for (int i = 0; i < 100; i++) {
			if (syncMPSSESingle()) return;
		}
		throw new RuntimeException("Could not sync MPSSE");
	}

	
	protected void cmd(int cmd, int[] toSend, int[] toRecv) throws IOException
	{
		// System.out.printf("Command: %02X send=%d recv=%d\n", cmd, toSend == null ? 0 : toSend.length, toRecv == null ? 0 : toRecv.length);
		checkQueue();
		
		if (toSend == null) toSend = new int[0];
		if (toRecv == null) toRecv = new int[0];
		
		byte[] tx = new byte[toSend.length + 2];
		byte[] rx = new byte[toRecv.length];
		tx[0] = (byte)cmd;
		tx[toSend.length + 1] = (byte)0x87;
		for (int i = 0; i < toSend.length; ++i)
			tx[i + 1] = (byte)toSend[i];
		
		write(tx);
		if (rx.length > 0) {
			read(rx);
			for (int i = 0; i < toRecv.length; ++i)
				toRecv[i] = 0xFF & rx[i];
		}
		
		// System.out.println(" -> Ok!");
	}
	

	/**
	 * Configures the FTDI clock.
	 * 
	 * @param divBy5		Whether or not to devide by 5
	 * @param clk3Phase		Use 3 phase clocking
	 * @param bitRate		Target bitrate
	 * @return				Actual bitrate
	 * @throws IOException
	 */
	public int cfgClock(boolean divBy5, boolean clk3Phase, int bitRate) throws IOException
	{
		cmd(CMD_DIS_ADAPT_CLK, null, null);
		int clkSpeed;		// Internal Clock speed
		int clkPerCycl;		// Clocks per bus cycle
		if (divBy5) {
			cmd(CMD_ENA_DIV5, null, null);	// enable div / 5
			clkSpeed = FT_FULLSPEED_CLK;
		} else {
			cmd(CMD_DIS_DIV5, null, null);	// disable div / 5
			clkSpeed = FT_HIGHSPEED_CLK;
		}
		
		if (clk3Phase) {
			cmd(CMD_ENA_CLK3PHASE, null, null);	// enable 3 phase clock
			clkPerCycl = 3;
		} else {
			cmd(CMD_DIS_CLK3PHASE, null, null);	// disable 3 phase clock
			clkPerCycl = 2;
		}

		
		int div = (clkSpeed / ( bitRate * clkPerCycl )) - 1;
		
		if (clkSpeed % (bitRate * clkPerCycl) != 0) {
			div ++;
		}
		
		if (div > 0xFFFF) div = 0xFFFF;
		cmd(CMD_SET_DIV, new int[] { div & 0xFF, (div & 0xFF00) >> 8 }, null);
		
		return (clkSpeed + ((div * clkPerCycl) / 2)) / (div * clkPerCycl);
		
	}

	public void setDrive0Only(int mask) throws IOException 
	{
		cmd(CMD_SET_DRIVE0ONLY, new int[] { mask & 0xFF, (mask >> 8) & 0xFF }, null);
	}
	
	public void setDataLowByte(int value, int direction) throws IOException 
	{
		cmd(CMD_SET_DATA_LOW_BYTE, new int[] { value, direction}, null);		
	}

	public void setDataHighByte(int value, int direction) throws IOException 
	{
		cmd(CMD_SET_DATA_HIGH_BYTE, new int[] { value, direction}, null);		
	}

	public void setLoopBack(boolean enable) throws IOException {
		if (enable) cmd(CMD_ENA_LOOPBACK, null, null);
		else cmd(CMD_DIS_LOOPBACK, null, null);
	}
	
	public int readDataLowByte() throws IOException {
		int[] ret = new int[1];
		cmd(CMD_READ_DATA_LOW_BYTE, null, ret);
		
		return ret[0];
	}
	public int readDataHighByte() throws IOException {
		int[] ret = new int[1];
		cmd(CMD_READ_DATA_HIGH_BYTE, null, ret);
		return ret[0];
	}


	

}
