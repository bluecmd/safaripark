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



/**
 * Controls the FTDI chip as SPI master.
 * 
 * This requires the chip to support MPSSE mode. 
 * 
 * Note that SPI mode0 and mode2 are supported. Mode1 and 3 are not.
 */
public class MPSSE_SPI extends BusBase {

	public static final int GPIOL0_INPUT = 0x0;
	public static final int GPIOL0_OUTPUT = 0x1;
	public static final int GPIOL1_INPUT = 0x0;
	public static final int GPIOL1_OUTPUT = 0x2;
	public static final int GPIOL2_INPUT = 0x0;
	public static final int GPIOL2_OUTPUT = 0x4;
	public static final int GPIOL3_INPUT = 0x0;
	public static final int GPIOL3_OUTPUT = 0x8;
	
	private static final int SPI_GPIO_MASK   = 0xF0;
	private static final int SPI_GPIO_SHIFT  = 4;
	
	private static final int SPI_CLK		 = 0x1;
	private static final int SPI_TDO		 = 0x2;
	private static final int SPI_TDI		 = 0x4;
	private static final int SPI_SS			 = 0x8;
	
	private static final int SPI_IO_IOCFG    = SPI_CLK | SPI_TDO | SPI_SS;		// 1011 (All output except DI)
	private static final int SPI_IO_DEFAULT  = SPI_TDO | SPI_TDI;
	
	
	
	private boolean _invClk;
	private int  	_gpioDir;
	
	private int   	_gpioState;
	/**
	 * Initializes the FTDI chip in MPSSE mode, as SPI master device.
	 * 
	 * @param dev					The FTDevice
	 * @param invClk				Invert clock (Mode 2, instead of Mode 0)
	 * @param bitRate				BitRate (0-30M)
	 * @param gpioDir				Direction of the 4 remaining GPIO ports, see GPIOLn_* defines.
	 */
	public MPSSE_SPI(MPSSE mpsse, boolean invClk, int bitRate, int gpioDir) throws IOException {
		super(mpsse);
		
		_gpioDir = ((gpioDir << SPI_GPIO_SHIFT) & SPI_GPIO_MASK) | SPI_IO_IOCFG;
		_gpioState = 0x0;
		_invClk = false;
		mpsse.switchMode(_gpioDir);
		// new MPSSEImpl(dev, _gpioDir);
		mpsse.cfgClock(false, false, bitRate);
		mpsse.setLoopBack(false);
		updateIO();
		
	}
	
	/**
	 * Returns one of the 4 GPIO bits.
	 * 
	 * @throws IOException 
	 */
	public boolean getIO(int bit) throws IOException {
		return (1 & (mpsse.readDataLowByte() >> (bit + SPI_GPIO_SHIFT))) != 0;
	}
	
	private void updateIO() throws IOException {
		
		mpsse.setDataLowByte(_gpioState | SPI_SS | (_invClk ? SPI_CLK : 0) | SPI_IO_DEFAULT, _gpioDir);
	}
	
	private int getQIO(boolean chipSelect) {
		return _gpioState | (chipSelect ? 0 : SPI_SS) | (_invClk ? SPI_CLK : 0) | SPI_IO_DEFAULT;
	}
	
	public void setIO(int bit, boolean set) throws IOException
	{
		if (set) _gpioState |= 1 << (bit + SPI_GPIO_SHIFT);
		else _gpioState &= ~ (1 << (bit + SPI_GPIO_SHIFT));
		
		updateIO();
	}
	
	/**
	 * Send data over SPI.
	 * @throws IOException 
	 */
	public void tx(byte[] data) throws IOException
	{
//		_chipSelect = true;
//		updateIO();
		int p = getQIO(true);
		int q = getQIO(false);
		dataShiftWIO(p, q, _gpioDir, JavaFTD2xxMPSSE.DSH_WRITE_TDI | (_invClk ? 0 : JavaFTD2xxMPSSE.DSH_MVE_CLK_WRITE), data.length, data, null);
//		_chipSelect = false;
//		updateIO();
	}

	public void dataShift(int dsh, int len, byte[] tx, byte[] rx) throws IOException 
	{
		mpsse.checkQueue();
		if ((dsh & ~0x7F) != 0) {
			throw new IOException("Invalid data shift command");
		}
		
		int chkLen = len;
		if ((dsh & MPSSE.DSH_MVE_BIT_MODE) != 0) {
			chkLen = (len + 7) / 8;	// bit mode
		}
		
		if (len > 0x10000) {
			throw new IOException("Length must be less or equal to 65536");
		}

		if ((dsh & MPSSE.DSH_WRITE_TDI) != 0) {
			if (tx == null) throw new IOException("Data Shift read must provide tx array");
			if (tx.length != chkLen) throw new IOException("Provide tx array not of correct length");
		}
		
		if ((dsh & MPSSE.DSH_READ_TDO) != 0) {
			if (rx == null) throw new IOException("Data Shift write must provide rx array");
			if (rx.length != chkLen) throw new IOException("Provide rx array not of correct length");
		}
		
		len -= 1;	// substact 1, (len 0 -> 1)
		
		byte[] cmd = { (byte)(dsh), (byte)(len & 0x00FF), (byte)((len & 0xFF00) >> 8) };
		
		mpsse.write(cmd);
		
		// TODO will not work for length > buffer size?
		if (tx != null) mpsse.write(tx);
		if (rx != null) mpsse.read(rx);
		
	}

	/* (non-Javadoc)
	 * @see org.nikhef.ftdi.ftd2xxj.MPSSE#dataShiftWIO(int, int, int, int, int, byte[], byte[])
	 */
	public void dataShiftWIO(int p, int q, int gpioDir, int dsh, int len, byte[] tx, byte[] rx) throws IOException 
	{
		mpsse.checkQueue();
		if ((dsh & ~0x7F) != 0) {
			throw new IOException("Invalid data shift command");
		}
		
		int chkLen = len;
		if ((dsh & MPSSE.DSH_MVE_BIT_MODE) != 0) {
			chkLen = (len + 7) / 8;	// bit mode
		}
		
		if (len > 0x10000) {
			throw new IOException("Length must be less or equal to 65536");
		}

		if ((dsh & MPSSE.DSH_WRITE_TDI) != 0) {
			if (tx == null) throw new IOException("Data Shift read must provide tx array");
			if (tx.length != chkLen) throw new IOException("Provide tx array not of correct length");
		}
		
		if ((dsh & MPSSE.DSH_READ_TDO) != 0) {
			if (rx == null) throw new IOException("Data Shift write must provide rx array");
			if (rx.length != chkLen) throw new IOException("Provide rx array not of correct length");
		}
		
		len -= 1;	// substact 1, (len 0 -> 1)
		
		byte[] txp = new byte[10 + chkLen];
		
		txp[0] = (byte)MPSSE.CMD_SET_DATA_LOW_BYTE;
		txp[1] = (byte)p;
		txp[2] = (byte)gpioDir;
		txp[3] = (byte)dsh;
		txp[4] = (byte)(len & 0x00FF);
		txp[5] = (byte)((len & 0xFF00) >> 8);
				
		if (tx != null) {
			System.arraycopy(tx, 0, txp, 6, chkLen);
		}
		txp[6 + chkLen] = (byte)MPSSE.CMD_SET_DATA_LOW_BYTE;
		txp[7 + chkLen] = (byte)q;
		txp[8 + chkLen] = (byte)gpioDir;
		txp[9 + chkLen] = (byte)0x87;
		
		mpsse.write(txp);
		if (rx != null) mpsse.read(rx);
	}
	
	
	public void rx(byte[] data) throws IOException
	{
//		_chipSelect = true;
//		updateIO();
		int p = getQIO(true);
		int q = getQIO(false);

		dataShiftWIO(p, q, _gpioDir, JavaFTD2xxMPSSE.DSH_READ_TDO | (_invClk ? 0 : JavaFTD2xxMPSSE.DSH_MVE_CLK_READ), data.length, null, data);
//		_chipSelect = false;
//		updateIO();
	}

	public void txrx(byte[] tx, byte[] rx) throws IOException
	{
//		_chipSelect = true;
//		updateIO();
		int p = getQIO(true);
		int q = getQIO(false);

		dataShiftWIO(p, q, _gpioDir, JavaFTD2xxMPSSE.DSH_READ_TDO | JavaFTD2xxMPSSE.DSH_WRITE_TDI | 
				(_invClk ? 0 : JavaFTD2xxMPSSE.DSH_MVE_CLK_WRITE), tx.length, tx, rx);
//		_chipSelect = false;
		//updateIO();
	}

	public void close() throws IOException {
		mpsse.close();
	}

	@Override
	protected void setGPIOChild(int port) {
		// TODO Auto-generated method stub
		
	}

}
