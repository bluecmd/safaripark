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

import nl.nikhef.sfp.i2c.I2CLink;
import nl.nikhef.sfp.i2c.SimI2CLink;
import nl.nikhef.sfp.i2c.VirtualI2CLink;

public class VirtualSFPDevice extends SFPDeviceBase {

	private final String _serial;
	private final int    _bays;
	private I2CLink[]	 _links;
	
	
	public VirtualSFPDevice(String serial, int bays) {
		_serial = serial;
		_bays = bays;
		_links = new I2CLink[_bays];


	}
	
	@Override
	public String getSerial() {
		return _serial;
	}

	@Override
	public int getBayCount() {
		return _bays;
	}

	@Override
	public I2CLink getLink(int bay) throws IOException {
		if (!isModulePresent(bay)) return null;
		return _links[bay];
	}

	@Override
	public boolean isModulePresent(int bay) {
		if (bay < 0 || bay > getBayCount()) return false;
		return _links[bay] != null;
	}
	


	@Override
	public void shutdown() {
	}

	@Override
	public void updateModules()
	{
		if (_links[0] != null) return;
		for (int bay = 0; bay < _links.length; bay++)
		{
			_links[bay] = new VirtualI2CLink(String.format("Virtual_%d", bay));
			sdlMgr.getProxy().sfpModuleStateChanged(this, bay);
		}
	}

	@Override
	public boolean isRxLoss(int bay) {
		return false;
	}

	@Override
	public boolean isTxFault(int bay) {
		return false;
	}

	
}
