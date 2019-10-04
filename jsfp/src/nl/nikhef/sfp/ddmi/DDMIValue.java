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
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import nl.nikhef.sfp.ddmi.DDMIMeta.BitFieldValue;
import nl.nikhef.tools.Utils;

public class DDMIValue extends DDMIElement {

	/**
	 * Fundemental data-type.
	 * 
	 * @author vincentb
	 */
	public enum DDMIType
	{
		/** Must be interpret as text string */
		TEXT_TYPE,
		/** Raw binary type, usually used for bit-masks */
		BITMAP_TYPE,
		/** Integer type, max 4 bytes, always signed */
		INT_TYPE,
		/** Integer type, max 4 bytes, always unsigned */
		UINT_TYPE,
		/** Unsigned fixed point */
		DECIMAL_TYPE_UFIXED,
		/** Signed fixed point */
		DECIMAL_TYPE_SFIXED,
		/** 32 bit floating point */
		DECIMAL_TYPE_FLOAT,
		/** Same as bitmap, but usually not visible */
		PASSWORD_TYPE		
	}	
	
	private int			_offset;
	private int			_length;
	private boolean		_writable;
	private boolean		_monitoring;
	private DDMIType	_type;
	private String 		_short;
	private int			_divider = 1;
	
	private ViewLevel _level;
	private Map<DDMIMeta<?>, Object> _meta;

	public DDMIValue(DDMIType type) {
		_type = type;
	}
	
	public DDMIType getType() {
		return _type;
	}
	
	public int getOffset() {
		return _offset;
	}
	
	public void setMonitor(boolean monitoring) {
		_monitoring = monitoring;
	}
	
	
	public boolean isMonitor() {
		return _monitoring;
	}

	public void setOffset(int offset) {
		_offset = offset;
	}

	
	public void setDivider(int divider) {
		_divider = divider;
	}
	
	public ViewLevel getLevel() {
		return _level;
	}

	public void setLevel(ViewLevel level) {
		_level = level;
	}

	/**
	 * @return the length
	 */
	public int getLength() {
		return _length;
	}

	/**
	 * @param length the length to set
	 */
	public void setLength(int length) {
		_length = length;
	}

	/**
	 * @return the writable
	 */
	public boolean isWritable() {
		return _writable;
	}

	/**
	 * @param writable the writable to set
	 */
	public void setWritable(boolean writable) {
		_writable = writable;
	}

	protected Object getMeta(DDMIMeta<?> m)
	{
		if (_meta == null || !hasMeta(m)) throw new NullPointerException("Meta does not exist");
		
		return _meta.get(m);
	}
	
	protected boolean hasMeta(DDMIMeta<?> m)
	{
		if (_meta == null) return false;
		return _meta.containsKey(m);
	}
	
	protected void setMeta(DDMIMeta<?> m, Object o)
	{
		if (_meta == null) _meta = new HashMap<DDMIMeta<?>, Object>();
		_meta.put(m, o);
	}

	/**
	 * @return the short
	 */
	public String getShort() {
		if (_short == null) return getLabel();
		return _short;
	}

	/**
	 * @param s the short to set
	 */
	public void setShort(String s) {
		_short = s;
	}
	
	public int getDivider() {
		return _divider;
	}
	


	private void lockUnlockField(DDMIContext ctx, boolean unlock) {
		if (getPasswordId() == null) return;	// there is no password
		
		DDMIValue el = DDMIValue.class.cast(ctx.getDDMI().getElementById(getPasswordId()));

		if (el == null) throw new RuntimeException("Password field with ID " + getPasswordId() + " not found");
		
		if (!DDMIMeta.CONST.partOf(el)) throw new RuntimeException("For now passowords must always have a constant");
		
		String s = DDMIMeta.CONST.of(el);
		byte[] password = Utils.hexStringToBytes(s);

		if (!unlock) Arrays.fill(password, (byte)0xFF);

		el.writeRaw(ctx, password);
	}

	
	public byte[] readRaw(DDMIContext ctx) {

		lockUnlockField(ctx, true);
		
		try {
			DataSource acc = ctx.getDDMI().getSourceById(getSourceId());

			if (acc != null) {
				return acc.read(ctx, _offset, _length);
			}
			throw new RuntimeException("Datasource with ID " + getSourceId() + " not found");
		} finally {
			lockUnlockField(ctx, false);		
		}
		
		// return new byte[_length];			
	}
	
	
	public void writeRaw(DDMIContext ctx, byte[] dta) {
		if (!_writable) throw new AccessException("Field is not writable");
		assert(dta.length == _length);
		
		lockUnlockField(ctx, true);
		
		try {
			DataSource acc = ctx.getDDMI().getSourceById(getSourceId());
			acc.write(ctx, _offset, dta);
		} finally {
			lockUnlockField(ctx, false);
		}
		
	}
	

	@Override
	public String toString() {
		return String.format("<Value '%s' type=%s>", getLabel(), getType());
	}
	
	@Override
	public void output(StringBuilder sb, DDMIContext ctx, int level) 
	{
		SFPUtils.pad(sb, level);
		sb.append(String.format("* %-24s: ", getLabel()));
		
		switch (_type) {
		case INT_TYPE:
			int v = DDMIUtils.readInt(this, ctx);
			if (DDMIMeta.LOOKUP.partOf(this)) {
				DDMIMeta.LookupTable lut = DDMIMeta.LOOKUP.of(this);
				sb.append(String.format("%s (%02d)\n", lut.get(v), v));
			} else {
				sb.append(String.format("%d\n", v));
			}
			
			break;
		case TEXT_TYPE:
			sb.append(DDMIUtils.readString(this, ctx));
			sb.append('\n');
			break;
		case BITMAP_TYPE:
			for (byte b : readRaw(ctx))
			{
				sb.append(String.format("%02x ", 0xFF & b));
				
			}
			sb.append('\n');
			if (DDMIMeta.BITFIELD.partOf(this)) {
				DDMIMeta.BitField bf = DDMIMeta.BITFIELD.of(this);
				for (BitFieldValue bfield : bf.fields()) {
					SFPUtils.pad(sb, level);
					sb.append(String.format("  - %s: %s\n", bfield.name, bfield.getStringValue(ctx, this)));
				}
			}
			break;
		default:
			for (byte b : readRaw(ctx))
			{
				sb.append(String.format("%02x ", 0xFF & b));
			}
			sb.append('\n');
			break;
		}
		
	}

	

	

}
