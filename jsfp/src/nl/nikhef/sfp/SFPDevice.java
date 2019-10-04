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
import java.util.Collection;

import nl.nikhef.sfp.i2c.I2CLink;

/**
 * Encapsulates the peripheral which reads the SFP device, not the SFP device itself.
 */
public interface SFPDevice extends Comparable<SFPDevice>
{
	/**
	 * Device serial.
	 */
	public String getSerial();

	/**
	 * Number of SFP 'bays' on the device.
	 * @return
	 */
	public int getBayCount();
	
	
	/**
	 * Get the serial number of the connected module.
	 * 
	 * @param bay	The bay number.
	 * 
	 * @return		the module identifier, or null if non is connected.
	 */
	public String getModuleName(int bay);
	
	/**
	 * Returns a I2CLink to the specified device.
	 * 
	 * @param bay			The bay number
	 * @return				The I2CLink
	 * @throws IOException	An IOException if the link could not be made
	 */
	public I2CLink getLink(int bay) throws IOException;
	
	/**
	 * Returns whether or not a specific module is present.
	 * 
	 * @param bay	The bay number
	 * @return		true - its present, false - its not present.
	 */
	public boolean isModulePresent(int bay);

	/**
	 * Returns whether or not the RX loss signal is asserted.
	 * 
	 * @param bay	The bay number
	 * @return		true is RX loss is asserted, false otherwise.
	 */
	public boolean isRxLoss(int bay);

	/**
	 * Returns whether or not the TX fault signal is asserted
	 * 
	 * @param bay	The bay number
	 * @return		true - the signal is asserted, false otherwise
	 */
	public boolean isTxFault(int bay);
	
	/**
	 * Forces the module information to be reloaded.
	 * 
	 * @param bay	The bay to reload information from.
	 */
	public void refreshInfo(int bay);
	
	/**
	 * 
	 * @param sdl
	 */
	public void addDeviceListener(SFPDeviceListener sdl);
	
	public Collection<SFPDeviceListener> getDeviceListeners();
	
	public void removeDeviceListener(SFPDeviceListener sdl);

	/**
	 * Closes the device.
	 */
	public void shutdown();
	
	/**
	 * Call invoked by provider each second to check the module state.
	 */
	public void updateModules();

	/**
	 * Returns whether or not this module has digital diagnostics.
	 */
	public boolean hasDiagnostics(int bay);

	
	
}
