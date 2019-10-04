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

import nl.nikhef.sfp.ddmi.SFPUtils;

public class SimI2CLink implements I2CLink {

	private int _eepromPtr = 0;
	private int _diagPtr = 0;
	
	
	private byte[] _eeprom = new byte[] {
		0x03, 0x04, 
		0x00,	// Connector values, SFF-8024
		0x00, 0x00, 0x00, 0x08, 0x00, 0x00, 0x00, 0x00,
		0x00,	// Encoding, SFF-8024
		0x64,	// 10 GB
		0x0E,	// Rate ID
		0x0D, 0x00, 0x00, 0x00, 0x00, 0x64,	// Lengths
		(byte)'N',(byte)'I',(byte)'K',(byte)'H',	// Vendor name
		(byte)'E',(byte)'F',(byte)' ',(byte)' ',
		(byte)' ',(byte)' ',(byte)' ',(byte)' ',
		(byte)' ',(byte)' ',(byte)' ',(byte)' ',
		0x00,	// Transciever (B)
		0x00, 0x00, 0x00,	// Vendor OUD
		(byte)'0',(byte)'1',(byte)'2',(byte)'3',	// Vendor part number
		(byte)'4',(byte)'5',(byte)'S',(byte)'F',
		(byte)'P',(byte)' ',(byte)' ',(byte)' ',
		(byte)' ',(byte)' ',(byte)' ',(byte)' ',
		(byte)'1',(byte)'.',(byte)'0',(byte)'1',	// Revision
		0x00, 0x00,									// Wavelength
		0x00,										// reserved\
		0x00,				// CHECKSUM
		0x00, 0x00,			// Optional transceiver values
		0x00, 0x00,			// BR-max, BR-min
		(byte)'0',(byte)'0',(byte)'1',(byte)'1',	// Vendor serial number
		(byte)'2',(byte)'2',(byte)'3',(byte)'3',
		(byte)'A',(byte)'B',(byte)'C',(byte)'D',
		(byte)'B',(byte)'A',(byte)'D',(byte)'D',
		(byte)'1',(byte)'6',(byte)'0',(byte)'1',	// Vendor date code
		(byte)'1',(byte)'4',(byte)'0',(byte)'0',
		0x40, // Diagnostic monitoring type
		0x00, // Enhanced options
		0x08, // SFF-8472 compliance
		0x00,	// CHECKSUM	// 96
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	// 112
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	// 128
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	// 144
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	// 160
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	// 176
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	// 192
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	// 208
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	// 224
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	// 240
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 	// 256
	};

	private byte[] _diag = new byte[] {
		// Thresholds
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // Temperature
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // Voltage
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // Bias
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // TX Power
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // RX Power
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // Laser temp
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, // TEC current
		// RX Power poly (32 bit fp)
		0x00, 0x00, 0x00, 0x00, // arg 0	
		0x00, 0x00, 0x00, 0x00,	// arg 1
		0x00, 0x00, 0x00, 0x00,	// arg 2
		0x00, 0x00, 0x00, 0x00,	// arg 3
		0x00, 0x00, 0x00, 0x00,	// arg 4
		
		0x00, 0x00, 0x00, 0x00,	// TX bias current line, slope & offset
		0x00, 0x00, 0x00, 0x00,	// TX power line, slope & offset
		0x00, 0x00, 0x00, 0x00,	// Temperature line, slope & offset
		0x00, 0x00, 0x00, 0x00,	// Voltage line, slope & offset
		0x00, 0x00, 0x00, 0x00,	// 3x reserved, 1x checksum
	
		// ADC values
		0x00, 0x00, 			// Temperature ADC value
		0x00, 0x00, 			// Vcc ADC value
		0x00, 0x00, 			// TX Bias ADC value
		0x00, 0x00, 			// TX Power ADC value
		0x00, 0x00, 			// RX Power ADC value
		0x00, 0x00, 			// Laser temp/wavelength ADC value
		0x00, 0x00, 			// TEC current
		0x00, 0x00,				// Optional status	/control

		// Misc.
		0x00, 0x00,				// Reserved warning/alarm flags
		0x00, 0x00,				// Input equalization, output emphasis
		0x00, 0x00,				// more warning/alarm flags
		0x00, 0x00,				// extended module control/status
		// Vendor specific
		0x00, 0x00, 0x00, 0x00, 
		0x00, 0x00, 0x00, 
		
		// page select
		0x00,

		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	// 128
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	// 144
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	// 160
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	// 176
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	// 192
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	// 208
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	// 224
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	// 240
		0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00 	// 256
		
	};


	
	public SimI2CLink(String serial) {

		byte[] serialBytes = serial.getBytes();
		for (int i = 0; i < 16; i++) {
			_eeprom[i + 68] = i < serialBytes.length ? serialBytes[i] : 0x20;
		}
		
		_eeprom[63] = SFPUtils.checkSum(_eeprom, 0, 63);
		_eeprom[95] = SFPUtils.checkSum(_eeprom, 64, 31);
		_diag[95]   = SFPUtils.checkSum(_diag, 0, 95);
	}

	
	@Override
	public void open() throws IOException {
	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public void i2cWrite(int addr, byte[] data) throws IOException 
	{
		if (addr != 0x50 && addr != 0x51) throw new IOException("I2C No Ack");
		/*
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		 */
		if (addr == 0x50) {
			_eepromPtr = 0xFF & data[0];
		
			for (int i = 1; i < data.length; i++)
			{
				_eeprom[_eepromPtr++] = data[i];
			}
		}
			
		if (addr == 0x51) 
		{
			_diagPtr = 0xFF & data[0];

			for (int i = 1; i < data.length; i++)
			{
				_diag[_diagPtr++] = data[i];
			}
		}
		
		
	}

	
	
	@Override
	public byte[] i2cRead(int addr, int len) throws IOException {
		
		if (addr != 0x50 && addr != 0x51) throw new IOException("I2C No Ack");

		/*
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		*/
		
		byte[] d = new byte[len];
		for (int i = 0; i < len; i++)
		{
			if (addr == 0x50) d[i] = _eeprom[_eepromPtr++];
			if (addr == 0x51) d[i] = _diag[_diagPtr++];
		}
		return d;
	}

	
	
	@Override
	public byte[] i2cWrRd(int addr, byte[] dataWr, boolean contRd, int rdLen)
			throws IOException {
		
		i2cWrite(addr, dataWr);
		
		return i2cRead(addr, rdLen);
	}

	@Override
	public void shutdown() throws IOException {
	}

}
