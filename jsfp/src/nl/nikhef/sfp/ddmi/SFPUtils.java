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
package nl.nikhef.sfp.ddmi;

import java.util.Arrays;
import java.util.regex.Pattern;


public final class SFPUtils {

	private SFPUtils() {
	}
	
	public static final byte checkSum(byte[] data, int off, int len) {
		int sum = 0;
		
		for (int i = off; i < off+len; ++i) {
			sum += 0xFF & data[i];
		}
		return (byte)(sum & 0xFF);
	}
	
	private static final Pattern CONDITION = Pattern.compile(
			"(?<id>[a-zA-Z0-9_]+):(?<bit>\\d+)"); 
	

	public static int getBit(byte[] bitmap, int pos) {
		
		int byt_pos = pos >> 3;
		int bit_pos = pos & 0x7;
		
		if (byt_pos >= bitmap.length)
			throw new RuntimeException(String.format("Bit at position %d does not exist for bitmap", pos));
		
		
		
		return (bitmap[byt_pos] >> bit_pos) & 0x1;
	}

	public static void pad(StringBuilder sb, int level) {
		char [] padding = new char[level * 2];
		Arrays.fill(padding, ' ');
		sb.append(padding);
	}
}
