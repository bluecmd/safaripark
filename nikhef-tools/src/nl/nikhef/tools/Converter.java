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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts one type to another type.
 * 
 * @author vincentb
 */
public final class Converter {

	private Converter() {
	}
	
	
	private static class ConvCacheKey
	{
		public final Class<?> from;
		public final Class<?> to;
		
		public ConvCacheKey(Class<?> from, Class<?> to)
		{
			this.from  = from;
			this.to = to;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((from == null) ? 0 : from.hashCode());
			result = prime * result + ((to == null) ? 0 : to.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof ConvCacheKey))
				return false;
			ConvCacheKey other = (ConvCacheKey) obj;
			if (from == null) {
				if (other.from != null)
					return false;
			} else if (!from.equals(other.from))
				return false;
			if (to == null) {
				if (other.to != null)
					return false;
			} else if (!to.equals(other.to))
				return false;
			return true;
		}
		
		
	}
	
	private static List<Conversion> CONVERTERS = new ArrayList<Conversion>();
	
	
	private static final Map<ConvCacheKey, Conversion> CACHE = new HashMap<ConvCacheKey, Conversion>();
	
	static {
		CONVERTERS.add(new Conversion(String.class, int.class){
			@Override
			public Object convert(Object from) {
				String str = String.class.cast(from);
				if (str.toLowerCase().startsWith("x")) {
					return Integer.parseInt(str.substring(1), 16);
				} else {
					return Integer.parseInt(str);
				}
			}
		});
		CONVERTERS.add(new Conversion(String.class, boolean.class) {
			public Object convert(Object from) {
				return Boolean.parseBoolean(String.class.cast(from));
			}
		});
	}

	/**
	 * Add a new conversion.
	 * 
	 * @param c		The conversion to add.
	 */
	public static void add(Conversion c) {
		CONVERTERS.add(c);
		CACHE.clear();
	}
	
	/**
	 * Convert the provided input object to an instance of he provided output class.
	 * 
	 * @throws	RuntimeException		If the conversion can not be performed.
	 * 
	 * @param input		Input object
	 * @param to		Output class
	 * @return			Output object
	 */
	@SuppressWarnings("unchecked")
	public static <T> T convert(Object input, Class<T> to)
	{
		

		Class<?> from = input.getClass();
		
		Object output;
		if (from.equals(to)) 
		{
			output = input;
		}
		else
		{
			Conversion conv = null;
			
			for (Conversion c : CONVERTERS)
			{
				if (c.from.isAssignableFrom(from) && to.isAssignableFrom(c.to)) {
					conv = c;
					break;
				}
			}
			
			if (conv == null) 
				throw new RuntimeException(
						String.format("Can't convert from %s to %s", from.getSimpleName(), to.getSimpleName()));
			output = conv.convert(input);

		}
		
		if (to.isPrimitive()) 
		{	// can't do checked casts for primitives.
			return (T)output;
		} else {
			return to.cast(output);
		}
	}
	
	
}
