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

/**
 * Generic I2C interface.
 * 
 * 
 * 
 * @author vincentb
 */
public interface I2CLink {

	/**
	 * Open the I2C link. Called before a batch of I2C transactions.
	 * 
	 * @throws IOException	If an I2C bus exception, or interface error occurs
	 */
	public void open() throws IOException;
	
	
	/**
	 * Close the I2C link. Called after a batch of I2C transactions.
	 * 
	 * Note: The 
	 * 
	 * @throws IOException	If an I2C bus exception, or interface error occurs
	 */
	public void close() throws IOException;
	
	/**
	 * Close the link and never open again.
	 * 
	 * @throws IOException
	 */
	public void shutdown() throws IOException;
	
	/**
	 * Write data to the I2C bus. 
	 * 
	 * @param addr		The I2C address without the r/w bit
	 * @param data		The data to write.
	 * 
	 * @throws IOException	If an I2C bus exception, or interface error occurs
	 */
	public void i2cWrite(int addr, byte[] data) throws IOException;
	
	/**
	 * Read from the I2C bus.
	 * 
	 * @param addr		The I2C address without the r/w bit
	 * @param len		Number of bytes to read
	 * @return			The data read.
	 * 
	 * @throws IOException	If an I2C bus exception, or interface error occurs
	 */
	public byte[] i2cRead(int addr, int len) throws IOException;
	
	/**
	 * Write then read from I2C bus.
	 * 
	 * @param addr		The I2C address
	 * @param dataWr	The data to write
	 * @param contRd	Whether or not to use continued read (advise)
	 * @param rdLen		No of bytes to read
	 * 
	 * @return			The data read.
	 * 
	 * @throws IOException	If an I2C bus exception, or interface error occurs
	 */
	public byte[] i2cWrRd(int addr, byte[] dataWr, boolean contRd, int rdLen) throws IOException;
}
