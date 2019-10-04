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
package nl.nikhef.sfp;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;


import nl.nikhef.rebus.dev.PCA9848;
import nl.nikhef.rebus.ftdi.MPSSE_GPIO;
import nl.nikhef.rebus.ftdi.MPSSE_I2C;
import nl.nikhef.rebus.ftdi.FTDIDevice;
import nl.nikhef.rebus.ftdi.FTDIEnumerator;
import nl.nikhef.rebus.ftdi.MPSSE;
import nl.nikhef.sfp.i2c.I2CLink;

public class MultiSFPDevice extends SFPDeviceBase {


	private static final Logger LOG = Logger.getLogger(MultiSFPDevice.class.getSimpleName());
	
	private static final int PCA_RESET	 = MPSSE_I2C.GPIO_AL0;
	private static final int ACTIVE_LED	 = MPSSE_I2C.GPIO_AL1;
	private static final int TX_LED		 = MPSSE_I2C.GPIO_AL2;
	private static final int RX_LED		 = MPSSE_I2C.GPIO_AL3;
	
	private static final int MODULE_PRESENT_IOS   = MPSSE_GPIO.GPIO_AL0 | MPSSE_GPIO.GPIO_AL1 | MPSSE_GPIO.GPIO_AL2 | MPSSE_GPIO.GPIO_AL3;
	private static final int MODULE_PRESENT_SHIFT = 4;
	
	private static final int RX_LOSS_IOS   = MPSSE_GPIO.GPIO_AH0 | MPSSE_GPIO.GPIO_AH1 | MPSSE_GPIO.GPIO_AH2 | MPSSE_GPIO.GPIO_AH3;
	private static final int RX_LOSS_SHIFT = 8;
	private static final int TX_FAULT_IOS   = MPSSE_GPIO.GPIO_AH4 | MPSSE_GPIO.GPIO_AH5 | MPSSE_GPIO.GPIO_AH6 | MPSSE_GPIO.GPIO_AH7;
	private static final int TX_FAULT_SHIFT = 12;
	
	private static final int OUTPUT_MASK = ACTIVE_LED | TX_LED | RX_LED;
	
	private static final int NO_OF_BAYS = 4;
	private FTDIDevice	_portA;	// FTDI Port A device
	private FTDIDevice	_portB;	// FTDI Port B device
	private String 		_serial;
	private MPSSE_I2C 		_i2c;
	private MPSSE_GPIO		_gpio;
	private MPSSE 		_mpsseA;
	private MPSSE 		_mpsseB;
	private PCA9848 	_mux;
	private int 		_selected = -1;
	private I2CLink[] _links = new I2CLink[NO_OF_BAYS];
	private boolean     _shutdown = false;
	private Object		_monitor;
	
	private int _modulePresentMask = 0;
	private int _txFaultMask = 0;
	private int _rxLossMask = 0;
	
	public MultiSFPDevice(String ftdiSerial, Object monitor) throws IOException 
	{
		_serial = ftdiSerial;
		
	//	LOG.setLevel(Level.FINE);
		LOG.fine("Created in SFP devce");
		
		_monitor = monitor;
		
	
		_portA = FTDIEnumerator.getDefault().getBySerialNumber(ftdiSerial + "A");
		_portB = FTDIEnumerator.getDefault().getBySerialNumber(ftdiSerial + "B");
		
		LogManager.getLogManager().getLogger("com.ftdi.FTDevice").setLevel(Level.WARNING);
		
		_mpsseA = _portA.createMPSSE();
		_mpsseB = _portB.createMPSSE();
		_i2c = new MPSSE_I2C(_mpsseA, MPSSE_I2C.SPEED_NORMAL, false);
		_i2c.setModes(OUTPUT_MASK | PCA_RESET, OUTPUT_MASK | PCA_RESET | RX_LOSS_IOS | TX_FAULT_IOS);
		
		// Reset PCA9848
		_i2c.setOutputs(0, PCA_RESET);
		_i2c.setOutputs(PCA_RESET, PCA_RESET);
		
		_gpio = new MPSSE_GPIO(_mpsseB);
		_gpio.setModes(0, MODULE_PRESENT_IOS);

		setLeds(false, false);
		// _i2c.setOutputs(0xF0, 0xE0);
		_i2c.wakeI2C();
		
		_mux = new PCA9848(_i2c, PCA9848.ADDRESS_HHH);
		_mux.setSingle(-1);
		for (int i = 0; i < _links.length; ++i) 
		{
			_links[i] = new MultiSFPI2CLink(i);			
		}
	}
	
	
	@Override
	public String getSerial() {
		return _serial;
	}

	@Override
	public int getBayCount() {
		return NO_OF_BAYS;
	}

	@Override
	public I2CLink getLink(int bay) throws IOException {
		return _links[bay];
	}

	@Override
	public boolean isModulePresent(int bay) {
		return ((_modulePresentMask >> bay) & 0x1) != 0;
	}
	
	@Override
	public boolean isTxFault(int bay) {
		return ((_txFaultMask >> bay) & 0x1) != 0;
	}
	
	@Override
	public boolean isRxLoss(int bay) {
		return ((_rxLossMask >> bay) & 0x1) != 0;
	}


	private void setLeds(boolean tx, boolean rx) throws IOException
	{
		int leds = ACTIVE_LED;
		if (tx) leds |= TX_LED;
		if (rx) leds |= RX_LED;
		
		_i2c.setOutputs(leds, OUTPUT_MASK);
	}
	
	private void select(int bay) throws IOException {
		if (_selected == bay) return;
		try {
			LOG.fine(String.format("Select bay %d", bay));
			synchronized (_monitor) {
				_mux.setSingle(bay);	
			}
		} catch (IOException ioe) {
			_selected = -1;
			throw ioe;
		}
		_selected = bay;
	}
	
	private void updateIOState() throws IOException
	{
		int nowPresent;
		try {
			synchronized(_monitor) {
				nowPresent = 0xF ^ (_gpio.getInputs(MODULE_PRESENT_IOS) >> MODULE_PRESENT_SHIFT);
			}
		} catch (IOException io)
		{
			nowPresent = 0;			
		}
		
		int changes = nowPresent ^ _modulePresentMask;
		
		_modulePresentMask = nowPresent;
		int lossAndFault = _i2c.getInputs(RX_LOSS_IOS | TX_FAULT_IOS);
		_rxLossMask = ((lossAndFault >> RX_LOSS_SHIFT) & 0xF);
		_txFaultMask = ((lossAndFault >> TX_FAULT_SHIFT) & 0xF);
		
		// System.out.printf("Present: %02x\n", _modulePresentMask);
		
		if (changes != 0) {
			for (int i = 0; i < NO_OF_BAYS; ++i)
			{
				if ((changes & 1 << i) != 0) {
					sdlMgr.getProxy().sfpModuleStateChanged(this, i);
				}
			}
		}
		
	}


	@Override
	public void updateModules() {
		try {
			updateIOState();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private  byte[] i2cWrRd(int addr, byte[] dataWr, boolean contRd, int rdLen) throws IOException {
		synchronized(_monitor) {
			setLeds(true, true);
			byte[] dta = _i2c.writeRead(addr, dataWr, rdLen);
			setLeds(false, false);
			return dta;
			}
	}
	
	private  void i2cWrite(int addr, byte[] data) throws IOException {
		synchronized(_monitor) {
			setLeds(true, false);
			_i2c.write(addr, data);
			setLeds(false, false);
		}
	}

	private synchronized byte[] i2cRead(int addr, int len) throws IOException {
		synchronized(_monitor) {
			setLeds(false, true);
			byte[] dta = _i2c.read(addr, len);
			setLeds(false, false);
			return dta;
		}
	}
	
		
	
	private class MultiSFPI2CLink implements I2CLink {

		private final int _muxPort;
		
		public MultiSFPI2CLink(int muxPort) {
			_muxPort = muxPort;
		}

		@Override
		public void open() throws IOException 
		{
			select(_muxPort);									
		}

		@Override
		public void close() throws IOException {
			// select(-1);
		}

		@Override
		public void shutdown() throws IOException {
		}

		public void checkSelected() throws IOException
		{
			if (_muxPort != _selected) {
				throw new IOException(String.format("Device bay %d is not selected, %d is", _muxPort, _selected));				
			}
		}
		
		@Override
		public void i2cWrite(int addr, byte[] data) throws IOException {
			checkSelected();
			MultiSFPDevice.this.i2cWrite(addr, data);
		}

		@Override
		public byte[] i2cRead(int addr, int len) throws IOException {
			checkSelected();
			return MultiSFPDevice.this.i2cRead(addr, len);
		}

		@Override
		public byte[] i2cWrRd(int addr, byte[] dataWr, boolean contRd, int rdLen) throws IOException {
			checkSelected();
			return MultiSFPDevice.this.i2cWrRd(addr, dataWr, contRd, rdLen);
		}
		
	}



	@Override
	public void shutdown() 
	{
		if (_shutdown) return;
		_shutdown = true;
		
		int wasPresent = _modulePresentMask;

		_modulePresentMask = 0;
		
		if (wasPresent != 0) {
			for (int i = 0; i < NO_OF_BAYS; ++i)
			{
				if ((wasPresent & 1 << i) != 0) {
					sdlMgr.getProxy().sfpModuleStateChanged(this, i);
				}
			}
		}
		

		try {
			_mpsseA.close();
		} catch (IOException e) {
		}
		try {
			_mpsseB.close();
		} catch (IOException e) {
		}
	}
}
