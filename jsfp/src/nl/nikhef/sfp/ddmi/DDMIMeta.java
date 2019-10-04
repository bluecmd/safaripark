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

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class DDMIMeta<T extends Object> {

	private Class<T> _clazz;
	
	public DDMIMeta(Class<T> clazz) {
		_clazz = clazz;
	}
	

	public boolean partOf(DDMIValue f) {
		return f.hasMeta(this);
	}

	
	public T of(DDMIValue f) {
		if (!partOf(f)) {
			try {
				f.setMeta(this, _clazz.newInstance());
			} catch (InstantiationException e) {
			} catch (IllegalAccessException e) {
			}
		}
		return _clazz.cast(f.getMeta(this));
	}

	
	
	@SuppressWarnings("serial")
	public static class LookupTable extends TreeMap<Integer, String> 
	{

		@Override
		public String get(Object o) 
		{
			if (super.containsKey(o)) return super.get(o);
			
			return String.format("Unknown <%s>", o);
		}
		
	}
	
	public static final class Scale {
		
		private float _scale = 1;
		private float _offset = 0;
		
		public Scale() {
			// System.out.println("Conversion created, no args");
		}
		
		public void setScaling(float scale, float offset)
		{
			_scale = scale;
			_offset = offset;
		}
		
		public Scale(float scale, float offset)
		{
			this._scale = scale;
			this._offset = offset;
		}

		public float apply(float value) 
		{
			return (value - _offset) * _scale;
		}
		
		public float revert(float value) 
		{
			return (value / _scale) + _offset;
		}

	}
	
	public static class BitFieldValue
	{
		public final int 	offset;
		public final int 	length;
		public final String name;
		public final String[] named;
		
		public BitFieldValue(String name, int offset, int length, String ...named) {
			this.name   = name;
			this.offset = offset;
			this.length   = length;
			this.named  = named;
		}

		
		public int getIntegerValue(DDMIContext ctx, DDMIValue field) 
		{
			int v = 0;
			
			byte[] rawValue = field.readRaw(ctx);
			
			for (int b = 0; b < length; b++) {
				v |= SFPUtils.getBit(rawValue, b + offset) << b;				
			}
			
			return v;
		}

		public String getStringValue(DDMIContext ctx, DDMIValue field) {
			
			int i = getIntegerValue(ctx, field);
			
			if (named == null || i >= named.length) {
				return Integer.toString(i);
			}
			
			return named[i];
		}

	}

	public static class BitField {
		
		
		private Map<Integer, BitFieldValue> _bitFieldValue = new TreeMap<Integer, BitFieldValue>();
		
		public void setBit(String name, int pos, boolean inverted)
		{
			setNamed(name, pos, 1, "False", "True");			
		}
		

		public void setValue(String name, int offset, int length)
		{
			setNamed(name, offset, length);			
		}

		public void setNamed(String name, int offset, int length, String ... named) 
		{
			_bitFieldValue.put(offset, new BitFieldValue(name, offset, length, named));									
		}
		
		public Collection<BitFieldValue> fields() {
			return _bitFieldValue.values();
		}
		
	}
	
	public static final DDMIMeta<BitField> BITFIELD = new DDMIMeta<BitField>(BitField.class);
	
	public static final DDMIMeta<LookupTable> LOOKUP = new DDMIMeta<LookupTable>(LookupTable.class);

	public static final DDMIMeta<String> UNIT = new DDMIMeta<String>(String.class);
	
	public static final DDMIMeta<Scale> CONV = new DDMIMeta<Scale>(Scale.class);

	public static final DDMIMeta<Integer> DEC_PLACES = new DDMIMeta<Integer>(Integer.class);
	
	public static final DDMIMeta<String> CONST = new DDMIMeta<String>(String.class);
	
	
}
