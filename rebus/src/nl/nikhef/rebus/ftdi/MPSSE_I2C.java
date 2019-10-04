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

import static nl.nikhef.rebus.ftdi.MPSSE.*;

public class MPSSE_I2C extends BusBase {

	private static final Logger LOG = Logger.getLogger(MPSSE_I2C.class.getSimpleName());	
	
	public static final int SPEED_SLOW = 10000;
	public static final int SPEED_NORMAL = 100000;
	public static final int SPEED_FAST = 400000;
	
	// Pins for clock, data in and data out
	private static final byte CK = 0x1;
	private static final byte DO = 0x2;
	private static final byte DI = 0x4;
	private static final byte CS = 0x8;

	private MPSSECmdBuilder _builder = new MPSSECmdBuilder();
	
	
	private byte[] _startSeq;
	private byte[] _rstartSeq;
	private byte[] _stopSeq;
	private boolean _regenSeqs = true;;


	/*
	 * AD0 -> TCK -----> SCL
	 * 
	 * AD1 -> TDI --+
	 *              |--> SDA
	 * AD2 -> TDO --+
	 */
	
	public MPSSE_I2C(MPSSE mpsse, int speed, boolean ft232h) throws IOException 
	{
		super(mpsse);
		
		// LOG.setLevel(Level.FINE);
		
		mpsse.switchMode(0);
		// configure clock speed
		mpsse.cfgClock(false, true, speed);
		
		if (ft232h) {
			// 	set pull down on ports AD0 1 and 2
			mpsse.setDrive0Only(0x7);
		}
		// LOG.setLevel(Level.FINE);
		LOG.fine(String.format("I2C initialized on MPSSE %s, myId=%08x", mpsse, System.identityHashCode(this)));
	}
	
	@Override
	protected void setGPIOChild(int portMask) throws IOException 
	{
		_regenSeqs = true;
		setBusIO(0xF, 0xB);
	}
	
	protected boolean llWrite(int b) throws IOException 
	{
		// set the builder to clear the buffer
		_builder.reset();

		// send the byte to write
		_builder.cmd(CMD_BYTE_OUT_P, 0, 0, b);
		
		// make DI/DO input, and force clock low
		_builder.cmd(CMD_SET_DATA_LOW_BYTE, (outMask & LO_A_MASK) | 0, (modeMask & LO_A_MASK) | CK);

		// clock in one bit
		_builder.cmd(CMD_BIT_IN_P, 0x0);
		
		// restore bus defaults
		_builder.cmd(CMD_SET_DATA_LOW_BYTE, (outMask & LO_A_MASK), (modeMask & LO_A_MASK) | CK | DO);

		// send it now
		_builder.cmd(CMD_SEND_IMMEDIATE);

		byte[] toRead = new byte[1];

		// execute and read response
		mpsse.write(_builder.getBytes());
		mpsse.read(toRead);

		// check for ACK
		return (toRead[0] & 0x01) == 0x00;				// read 1 byte, which is the bit result
	}
	

	protected void llDummy() throws IOException 
	{
		byte[] toWrite = {
			(byte)0x11, (byte)0x00, (byte)0x00,
			(byte)0xFF,
			0x13, 0x00, 0x00,
			(byte)0x80,
			(byte)((outMask & LO_A_MASK) | 0x0C), 
			(byte)((modeMask & LO_A_MASK) | 0x0B)
		};
		mpsse.write(toWrite);
	}
	
	protected int llRead(boolean nack) throws IOException {
		
		_builder.reset();
		
		// Disable data out for read
		_builder.cmd(CMD_SET_DATA_LOW_BYTE, (outMask & LO_A_MASK),  (modeMask & LO_A_MASK) | CK);
		// Clock in one byte
		_builder.cmd(CMD_BYTE_IN_P, 0x00, 0x00);
		// Enable data out
		_builder.cmd(CMD_SET_DATA_LOW_BYTE, (outMask & LO_A_MASK),  (modeMask & LO_A_MASK) | CK | DO);
		// Make NACK or ACK
		_builder.cmd(CMD_BIT_OUT_P, 0x00,  nack ? (byte)0xFF : 0x00);
		
		byte[] toRead = new byte[1];

		mpsse.write(_builder.getBytes());
		mpsse.read(toRead);

		return 0xFF & toRead[0];
	}
	

	protected byte[] llMRead(int bCount, boolean nackEnd) throws IOException {

		_builder.reset();
		for (int i = 0; i < bCount; i += 1)
		{
			// Disable data out for read
			_builder.cmd(CMD_SET_DATA_LOW_BYTE, (outMask & LO_A_MASK),  (modeMask & LO_A_MASK) | CK);
			// Clock in one byte
			_builder.cmd(CMD_BYTE_IN_P, 0x00, 0x00);
			// Enable data out
			_builder.cmd(CMD_SET_DATA_LOW_BYTE, (outMask & LO_A_MASK),  (modeMask & LO_A_MASK) | CK | DO);
			// Make NACK or ACK
			_builder.cmd(CMD_BIT_OUT_P, 0x00,  i == bCount - 1 && nackEnd ? (byte)0xFF : 0x00);
			
		}
		
		// set it low after a read
		_builder.cmd(CMD_SET_DATA_LOW_BYTE, (outMask & LO_A_MASK),  (modeMask & LO_A_MASK) | CK | DO);
		
		byte[] toRead = new byte[bCount];

		mpsse.write(_builder.getBytes());
		mpsse.read(toRead);
		
		return toRead;
	}
	
	
	protected boolean llAddr(int adr, boolean read) throws IOException {
		return llWrite((adr << 1) | ( read ? 1 : 0) );
	}

	
	protected void llStart() throws IOException
	{
		mpsse.write(_startSeq);
	}
	
	protected void llRStart() throws IOException
	{
		
		mpsse.write(_rstartSeq);		
	}

	
	protected void llStop() throws IOException
	{
		mpsse.write(_stopSeq);
		
	}

	
	protected void llIdle() throws IOException 
	{
		// Configure low port for I2C
		setBusIO(0xF, 0xB);
	}
	
	private byte[] genSeq(int m, int... x)
	{
		_builder.reset();
		
		int p = 0;
		for (int i = 0; i < x.length; i++)
		{
			for (int j = 0; j < m; j++) 
			{
				_builder.cmd(CMD_SET_DATA_LOW_BYTE, (outMask & LO_A_MASK) | x[i], (modeMask & LO_A_MASK) | CK | DO);
			}
		}
		return _builder.getBytes();
	}

	private void checkSequences()
	{
		if (!_regenSeqs) return;
		_startSeq = genSeq(150, CK | DO, CK, 0);
		_rstartSeq = genSeq(150, DO, CK | DO, CK, 0);
		_stopSeq = genSeq(150, CK, CK | DO);
		_regenSeqs = false;
	}
	
	public void wakeI2C() throws IOException {
		checkSequences();
		try {
			llStart();						// Start condition
			llDummy();
		} finally {
			llStop();
		}
		
	}
	
	public byte[] writeRead(int adr, byte[] writeDta, int readLen) throws IOException {
		mpsse.checkQueue();
		LOG.fine(String.format("WriteRead adr=%x, writeLen=%d, readLen=%d (instance=%08x)", adr, writeDta.length, readLen, System.identityHashCode(this)));
		checkSequences();
		try {
			llStart();						// Start condition
			if (!llAddr(adr, false)) {
				LOG.fine(" -> No ack during write @ device");
				throw new IOException(String.format("No ACK during address for write, device=%02x", adr));
			}
			for (byte b : writeDta) {
				if (!llWrite(b)) {
					LOG.fine(" -> No ack during write char");
					throw new IOException(String.format("No ACK during write, device=%02x, char=%02x", adr, b));
				}
			}
			// repeated start
			llRStart();
			if (!llAddr(adr, true)) {
				LOG.fine(" -> No ack during repeated start");
				throw new IOException(String.format("No ACK during address for read, device=%02x", adr));
			}
			byte[] readData = llMRead(readLen, true);
			return readData;
		} finally {
			llStop();
		}
	}

	public void write(int adr, byte[] data) throws IOException {
		mpsse.checkQueue();
		LOG.fine(String.format("Write adr=%x, writeLen=%d (instance=%08x)", adr, data.length, System.identityHashCode(this)));
		checkSequences();
		try {
			llStart();						// Start condition
			if (!llAddr(adr, false)) {
				throw new IOException(String.format("No ACK during address for write, device=%02x", adr));
			}
			for (byte b : data) {
				if (!llWrite(b)) 
					throw new IOException(String.format("No ACK during write, device=%02x, char=%02x", adr, b));
			}
		} finally {
			try {
				llStop();
			} catch (Exception e) {
			}
		}
	}

	public byte[] read(int adr, int len) throws IOException {
		mpsse.checkQueue();
		LOG.fine(String.format("Read adr=%x, writeLen=%d (instance=%08x)", adr, len, System.identityHashCode(this)));
		checkSequences();
		try {
			llStart();						// Start condition
			if (!llAddr(adr, true)) {
				throw new IOException(String.format("No ACK during address for read, device=%02x", adr));
			}
			/*byte[] readData = new byte[len];
			for (int i = 0; i < len; i++)
			{
				readData[i] = (byte)llRead(i == ( len - 1));
			}*/
			byte[] readData = llMRead(len, true);
			return readData;
		} finally {
			llStop();
		}
	}

	
	
}
