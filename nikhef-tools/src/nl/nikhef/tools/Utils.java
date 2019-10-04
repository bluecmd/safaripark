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

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;


public final class Utils {

	private Utils() {
	}

	
	@SafeVarargs
	public static <T extends Object> T[] arrayConcat(T[] first, T[]... rest) {
		int totalLength = first.length;
		for (T[] array : rest) {
			totalLength += array.length;
		}
		T[] result = Arrays.copyOf(first, totalLength);
		int offset = first.length;
		for (T[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}
	

	public static int[] arrayConcat(int[] first, int[]... rest) {
		int totalLength = first.length;
		for (int[] array : rest) {
			totalLength += array.length;
		}
		int[] result = Arrays.copyOf(first, totalLength);
		int offset = first.length;
		for (int[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}

	public static byte[] arrayConcat(byte[] first, byte[]... rest) {
		int totalLength = first.length;
		for (byte[] array : rest) {
			totalLength += array.length;
		}
		byte[] result = Arrays.copyOf(first, totalLength);
		int offset = first.length;
		for (byte[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}

	public static short[] arrayConcat(short[] first, short[]... rest) {
		int totalLength = first.length;
		for (short[] array : rest) {
			totalLength += array.length;
		}
		short[] result = Arrays.copyOf(first, totalLength);
		int offset = first.length;
		for (short[] array : rest) {
			System.arraycopy(array, 0, result, offset, array.length);
			offset += array.length;
		}
		return result;
	}
	

	public static void dumpHex(PrintWriter pw, byte[] data, boolean extended, int offset) {
		StringBuilder sbC = new StringBuilder();
		StringBuilder sbH = new StringBuilder();
		
		for (int i = 0; i < data.length; i++) {
			
			
			if (i % 16 == 0 && extended) {
				pw.printf("%04x: ", i + offset);
			}
			
			char c = (char)data[i];
			
			if (extended) sbC.append(c >= ' ' && c <=  '~' ? c : '.');
			sbH.append(String.format(" %02x", data[i]));
			if (i % 16 == 15) {
				if (extended) {
					pw.printf("%-16s %s\n", sbC, sbH);
					sbC.setLength(0);
				} else {
					pw.printf("%s\n", sbH);
				}
				
				sbH.setLength(0);
			}
		}
		
		if (sbC.length() > 0) {
			if (extended) {
				pw.printf("%-16s %s\n", sbC, sbH);
				sbC.setLength(0);
			} else {
				pw.printf("%s\n", sbH);
			}
		}
	}

	
	public static void dumpHex(String title, byte[] data) 
	{
		StringBuilder sbC = new StringBuilder();
		StringBuilder sbH = new StringBuilder();
		System.out.println(title);
		
		for (int i = 0; i < data.length; i++) {
			if (i % 16 == 0) {
				System.out.printf("%04x: ", i);
			}
			
			char c = (char)data[i];
			
			sbC.append(c >= ' ' && c <=  '~' ? c : '.');
			sbH.append(String.format(" %02x", data[i]));
			if (i % 16 == 15) {
				System.out.printf("%-16s %s\n", sbC, sbH);
				sbC.setLength(0);
				sbH.setLength(0);
			}
		}
		
		if (sbC.length() > 0) {
			System.out.printf("%-16s %s\n", sbC, sbH);
		}
	}


	public static <T extends Object> Iterable<T> filter(Collection<T> t, Filter<T> f)
	{
		return new FilteredIterable<T>(t, f);		

	}

	private static class FilteredIterator<T> implements Iterator<T> {

		
		
		private final Iterator<T> _it;
		private final Filter<T> _f;
		
		private T _cur = null;
		
		public FilteredIterator(Iterator<T> it, Filter<T> f) {
			_it = it;
			_f = f;
			findNext();
		}

		private void findNext() 
		{
			_cur = null;
			while (_it.hasNext()) {
				T check = _it.next();
				if (_f.match(check)) {
					_cur = check;
					break;
				}
			}
		}

		@Override
		public boolean hasNext() {
			return _cur != null;
		}

		@Override
		public T next() {
			T n = _cur;
			findNext();
			return n;
		}
	}

	
	private static class FilteredIterable<T> implements Iterable<T> {

		private final Collection<T> _t;
		private final Filter<T> _f;
		
		
		public FilteredIterable(Collection<T> t, Filter<T> f) {
			_t = t;
			_f = f;
			
		}

		@Override
		public Iterator<T> iterator() {
			return new FilteredIterator<T>(_t.iterator(), _f);
		}

	}
	
	final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

	public static String bytesToHexString(byte[] bytes) {
	    char[] hexChars = new char[bytes.length * 2];
	    for ( int j = 0; j < bytes.length; j++ ) {
	        int v = bytes[j] & 0xFF;
	        hexChars[j * 2] = hexArray[v >>> 4];
	        hexChars[j * 2 + 1] = hexArray[v & 0x0F];
	    }
	    return new String(hexChars);
	}
	
	public static byte[] hexStringToBytes(String s) {
	    int len = s.length();
	    if ((len & 1) != 0) return null;
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
}
