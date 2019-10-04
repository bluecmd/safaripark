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
package nl.nikhef.tools;

/**
 * Filter interface is for matching elements to the filter.
 * @author vincentb
 *
 * @param <T>
 */
public interface Filter<T extends Object> {

	/**
	 * Each object is provided to this class.
	 * 
	 * 
	 * @param other
	 * 
	 * 
	 * @return		Whether or not the object will be matched.
	 */
	public boolean match(T other);
	
	
	/**
	 * First elements are matched to the provided class.
	 */
	public Class<T> getFilterClass();
		
}
