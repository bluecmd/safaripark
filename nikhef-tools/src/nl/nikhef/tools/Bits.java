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
 * Simple utility to manipulate bits in a byte array.
 * 
 * @author vincentb
 */
public final class Bits {
	
	private Bits() {
	}

	public static boolean get(byte[] bytes, int pos) 
	{
		if (pos < 0) return false;
		
		int byPos = pos / 8;
		int biPos = pos % 8;
		if (byPos >= bytes.length) return false;
		
		return ((bytes[byPos] >> biPos) & 0x1) != 0;
	}

	public static void update(byte[] bytes, int pos, boolean selected) {
		if (pos < 0) return;
		int byPos = pos / 8;
		int biPos = pos % 8;
		if (byPos >= bytes.length) return;
		if (selected)	bytes[byPos] |= 1 << biPos;
		else	 		bytes[byPos] &= ~ ( 1 << biPos );
	}
	
}
