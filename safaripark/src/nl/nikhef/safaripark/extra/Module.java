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
package nl.nikhef.safaripark.extra;

import nl.nikhef.sfp.SFPDevice;

public class Module {

	public final SFPDevice dev;
	public final int bay;
	
	public Module(SFPDevice dev, int bay) {
		this.dev = dev;
		this.bay = bay;
	}
	
	public boolean isPresent()
	{
		return dev.isModulePresent(bay);
	}
	
	public boolean isTxFault() 
	{
		return dev.isTxFault(bay);
	}

	public boolean isRxLoss() 
	{
		return dev.isRxLoss(bay);
	}
	
	
	public String toString()
	{
		return dev.getModuleName(bay);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + bay;
		result = prime * result + ((dev == null) ? 0 : dev.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Module other = (Module) obj;
		if (bay != other.bay)
			return false;
		if (dev == null) {
			if (other.dev != null)
				return false;
		} else if (!dev.equals(other.dev))
			return false;
		return true;
	}

	public void refresh() {
		dev.refreshInfo(bay);
	}
	


	

}
