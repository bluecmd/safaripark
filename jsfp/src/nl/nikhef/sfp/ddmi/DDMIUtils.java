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

import nl.nikhef.sfp.ddmi.DDMIValue.DDMIType;

public final class DDMIUtils {
	
	private DDMIUtils() {
	}
	
	// -------------------------------------------------------------------------------------------
	//  Raw read/write conversion functions
	// -------------------------------------------------------------------------------------------

	
	public static final int rawToInt(byte[] rawData, DDMIValue val)
	{
		if (val.getLength() > 4) throw new AccessException("Can't interpret as integer");
		byte[] d = rawData;
		if (d == null) return 0;
		
		boolean signed;
		if (val.getType() == DDMIType.INT_TYPE ||
			val.getType() == DDMIType.DECIMAL_TYPE_SFIXED ||
			val.getType() == DDMIType.DECIMAL_TYPE_FLOAT) {
			signed =true;
		} else {
			signed = false;
		}
		
		int v = signed ? d[0] : d[0] & 0xFF;
		
		for (int i = 1; i < d.length; i++) {
			v <<= 8;
			v |= 0xFF & d[i];
		}

		return v;
	}
	
	public static final float rawToDecimal(DDMIValue val, byte[] rawData) 
	{
		float baseValue = Float.NaN;
		int intValue = rawToInt(rawData, val);

		switch (val.getType()) {
		case DECIMAL_TYPE_FLOAT:
			baseValue = Float.intBitsToFloat(intValue);
			break;
		case DECIMAL_TYPE_UFIXED:
		case DECIMAL_TYPE_SFIXED:
			baseValue = intValue;
			baseValue /= val.getDivider();
			break;
		default:
			break;
		}
		
		if (DDMIMeta.CONV.partOf(val)) {
			baseValue = DDMIMeta.CONV.of(val).apply(baseValue);
		}
		
		return baseValue;
		
	}

	public static final String rawToString(DDMIValue val, byte[] rawData) 
	{
		return new String(rawData).trim();
	}


	public static final byte[] decimalToRaw(DDMIValue val, float value) 
	{

		if (DDMIMeta.CONV.partOf(val)) {
			value = DDMIMeta.CONV.of(val).revert(value);
		}
		
		int intValue = 0;

		switch (val.getType()) {
		case DECIMAL_TYPE_FLOAT:
			intValue = Float.floatToIntBits(value);
			break;
		case DECIMAL_TYPE_UFIXED:
		case DECIMAL_TYPE_SFIXED:
			value *= val.getDivider();
			intValue = Math.round(value);
			break;
		default:
			break;
		}
		
		
		return intToRaw(val, intValue);

	}

	
	public static final byte[] intToRaw(DDMIValue val, int v) 
	{
		if (val.getLength() > 4) throw new AccessException("Can't interpret as integer");
		byte[] b = new byte[val.getLength()];
		for (int i = val.getLength() - 1; i >= 0; --i) {
			b[i] = (byte)(v & 0xFF);
			v >>= 8;
		}
		return b;
	}

	public static byte[] stringToRaw(DDMIValue val, String str) {
		byte[] newRaw = new byte[val.getLength()];
		
		byte[] strBytes = str.getBytes(); 
		
		for (int i = 0; i < newRaw.length; i++) {
			if (i < strBytes.length) {
				newRaw[i] = strBytes[i];
			} else {
				newRaw[i] = (byte)' ';
			}
		}
		
		return newRaw;
	}
	
	
	// -------------------------------------------------------------------------------------------
	//  Read functions
	// -------------------------------------------------------------------------------------------
	public static final String readString(DDMIValue val, DDMIContext ctx) 
	{
		return rawToString(val, val.readRaw(ctx));
	}
	
	public static final int readInt(DDMIValue val, DDMIContext ctx) 
	{
		return rawToInt(val.readRaw(ctx), val);
	}
	
	
	public static final float readDecimal(DDMIValue val, DDMIContext ctx) {
		return rawToDecimal(val, val.readRaw(ctx));
	}
	
	
	public static final String getValueAsSting(DDMIValue val, DDMIContext ctx) {
		switch (val.getType()) {
		case INT_TYPE:
			int v = readInt(val, ctx);
			if (DDMIMeta.LOOKUP.partOf(val)) {
				DDMIMeta.LookupTable lut = DDMIMeta.LOOKUP.of(val);
				return String.format("%s (%02d)", lut.get(v), v);
			} else {
				return String.format("%d", v);
			}
			
		case TEXT_TYPE:
			return readString(val, ctx);
		case DECIMAL_TYPE_FLOAT:
		case DECIMAL_TYPE_UFIXED:
		case DECIMAL_TYPE_SFIXED:
			return String.format("%.3f", readDecimal(val, ctx));
		default:
			StringBuilder sb = new StringBuilder();
			for (byte b : val.readRaw(ctx))
			{
				sb.append(String.format("%02x ", 0xFF & b));
				
			}
			return sb.toString();
		}
	}

	
	public static final void writeInt(DDMIValue val, DDMIContext ctx, int v) 
	{
		val.writeRaw(ctx, intToRaw(val, v));
	}

	
	

	

}
