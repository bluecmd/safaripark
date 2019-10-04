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
import java.util.ArrayList;
import java.util.List;

import com.ftdi.FTD2XXException;
import com.ftdi.FTDevice;

public abstract class FTDIEnumerator {
	
	public static FTDIEnumerator getDefault() {
		return DEFAULT_ENUMERATOR;
	}
	
	public abstract List<FTDIDevice> getDevices() throws IOException;
	
	
	
	private static class FTD2xxDevice implements FTDIDevice
	{
		
		private FTDevice _device;

		public FTD2xxDevice(FTDevice device) {
			_device = device;
		}

		@Override
		public String getSerial() {
			return _device.getDevSerialNumber();
		}

		@Override
		public FTDIType getType() {
			switch (_device.getDevType()){
			case DEVICE_232H: return FTDIType.FT_232H;
			case DEVICE_100AX: return FTDIType.FT_100AX;
			case DEVICE_2232C: return FTDIType.FT_100AX;
			case DEVICE_2232H: return FTDIType.FT_2232H;
			case DEVICE_232AM: return FTDIType.FT_232AM;
			case DEVICE_232BM: return FTDIType.FT_232BM;
			case DEVICE_232R: return FTDIType.FT_232R;
			case DEVICE_4232H: return FTDIType.FT_4232H;
			default:
				return FTDIType.FT_UNKNOWN;
			}
			
		}

		@Override
		public MPSSE createMPSSE() throws IOException {
			return new JavaFTD2xxMPSSE(_device);
		}

		@Override
		public String getDescription() {
			return _device.getDevDescription();
		}
		
	}
	
	private static class FTD2xxEnumerator extends FTDIEnumerator
	{

		@Override
		public List<FTDIDevice> getDevices() throws IOException {
			
			List<FTDIDevice> devices = new ArrayList<FTDIDevice>();
			
			try {
				for (FTDevice device : FTDevice.getDevices(true))
				{
					devices.add(new FTD2xxDevice(device));
				}
			} catch (FTD2XXException e) {
				throw new IOException("Failed to enumerate FTDI devices", e); 
			}
			
			return devices;
		}

		@Override
		public FTDIDevice getBySerialNumber(String serialNumber) throws IOException {
			
			try {
				return new FTD2xxDevice(FTDevice.getDevicesBySerialNumber(serialNumber).get(0));
			} catch (FTD2XXException e) {
				throw new IOException("Failed to get device", e);
			}
		}
		
	}
	
	private static FTDIEnumerator DEFAULT_ENUMERATOR = new FTD2xxEnumerator();

	public abstract FTDIDevice getBySerialNumber(String string) throws IOException;

}
