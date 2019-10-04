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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;

	
public abstract class DataSource {
	
	private final static int MAX_RETRIES = 20;
	
	private String _condition;
	private static final Logger LOG = Logger.getLogger(DataSource.class.getSimpleName());
	public final int start;
	public final int end;

	
	private class Checksum 
	{
		public final int offset;
		public final int start;
		public final int end;

		public Checksum(int offset, int start, int end) 
		{
			this.start  = start;
			this.offset = offset;
			this.end    = end;
		}

		public boolean check(DDMIContext ctx)
		{
			byte[] area = read(ctx, start, ( end - start ) + 1);
			byte sum = read(ctx, offset, 1)[0];
			return SFPUtils.checkSum(area, 0, ( end - start ) + 1) == sum; 
		}
		
		public void update(DDMIContext ctx)
		{
			byte[] area = null;
			
			for (int retry = 0; retry < MAX_RETRIES; retry++)
			{
				try {
					area = readMedia(ctx, start, ( end - start ) + 1);
				} catch (RuntimeException re) {
					if (retry == MAX_RETRIES - 1) throw re;
				}
			}
			if (area == null) return;
			
			write(ctx, offset, new byte[] { SFPUtils.checkSum(area, 0, ( end - start ) + 1) } );
		}
		
	}
	
	private class Cache 
	{
		public final int start;
		public final int end;
		public final String key;

		public Cache(int start, int end) 
		{
			// align cache to word size
			if ((start & 0x7) != 0) start = start & 0xFFFFFFF8;
			if ((end & 0x7) != 0x7) end = ((end + 0x7) & 0xFFFFFFF8) | 0x7;
			
			this.start  = start;
			this.end    = end;
			this.key = String.format("%s/%d:%d", _id != null ? _id : "obj:" + System.identityHashCode(DataSource.this), start, end);
		}

		public boolean appliesTo(int off, int len) {
			return off >= start && (off + len - 1) <= end;
		}

		
		
		public synchronized byte[]  read(DDMIContext ctx, int off, int len) {
			
			byte[] b = null; 
			if (!ctx.scratch.containsKey(key)) 
			{
				for (int retry = 0; retry < MAX_RETRIES; retry++)
				{
					try {
						b = readMedia(ctx, start, end - start + 1);
						break;
					} catch (RuntimeException re) {
						if (retry == MAX_RETRIES - 1) throw re; 					
						try {
							Thread.sleep(5);		
						} catch (InterruptedException ie) {
							// ignore
						}
					}
				}
				if (b == null) return null;
				ctx.scratch.put(key, b);				
			} else {
				b = (byte[])ctx.scratch.get(key);
			}
			
			return Arrays.copyOfRange(b, off - start, (off + len) - start);
		}

		public void write(DDMIContext ctx, int off, byte[] data) 
		{
			// only caches write within special boundaries
			int err = (off % 8);
			if (err != 0 || data.length % 8 != 0) 
			{
				int nOff = off - err;
				int nLen = data.length + err;
				if (nLen % 8 != 0) {
					nLen = nLen + (8 - (nLen % 8));
				}
				// System.out.printf("Rescaling write, old-off=%d new-off=%d old-len=%d new-len=%d\n", off, nOff, data.length, nLen);
				
				byte[] newData = null;
				
				for (int retry = 0; retry < MAX_RETRIES; retry++) 
				{
					newData = read(ctx, nOff, nLen);
					if (newData != null) break;
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
					}
				}
				if (newData == null) {
					throw new RuntimeException("Failed to cache EEPROM data");
				}
				System.arraycopy(data, 0, newData, err, data.length);
				
				off = nOff;
				data = newData;
			}
			
			if (ctx.scratch.containsKey(key)) {
				byte[] b = (byte[])ctx.scratch.get(key);
				System.arraycopy(data, 0, b, off - start, data.length);
			}
			
			// With EEPROM, write in increments
			for (int i = 0; i < data.length; i += 8)
			{
				for (int retry = 0; retry < MAX_RETRIES; retry++)
				{
					try {
						writeMedia(ctx, off + i, Arrays.copyOfRange(data, i, i + 8));
						break;
					} catch (RuntimeException re) {
						if (retry == MAX_RETRIES - 1) throw re; 					
						try {
							Thread.sleep(10);		
						} catch (InterruptedException ie) {
							// ignore
						}
					}
				}
				
			}
		}

		public void clear(DDMIContext ctx) {
			ctx.scratch.remove(key);
		}
		
	}

	private int 			_pageSelect = -1;
	private List<Checksum>	_checkSums;
	private List<Cache>		_caches;
	private String			_id;
	
	protected DataSource(int start, int end)
	{
		this.start = start;
		this.end = end;
	}
	
	public void setId(String id) {
		_id = id;
	}
	
	public String getId() {
		return _id;
	}
	
	/**
	 * Sets the page select position, for PageDataSources.
	 * 
	 * @param pos	The page select position.
	 */
	public void setPageSelect(int pos)
	{
		_pageSelect = pos;				
	}
	
	/**
	 * Returns the page select position, or -1 if there is none.
	 * 
	 * @return		The page select position.
	 */
	public int getPageSelect() 
	{
		return _pageSelect;		
	}
	
	/**
	 * Adds a checksum over the provided range.
	 * 
	 * Checksums are checked using the verifyChecksums() call and 
	 * written using the updateChecksums call.
	 * 
	 * @param offset	Position to which the checksum is written		
	 * @param start		Start of the checksum range, inclusive
	 * @param end		End of the checksum range, inclusive
	 */
	public void addChecksum(int offset, int start, int end)
	{
		if (offset < offset && offset > end)
			throw new RuntimeException("Checksum position can not be inside checksum itself!");
		
		if (_checkSums == null) _checkSums = new ArrayList<Checksum>();
		
		_checkSums.add(new Checksum(offset, start, end));
	}
	
	public boolean verifyChecksums(DDMIContext ctx)
	{
		if (!isValid(ctx)) return true; 
		if (_checkSums == null) return true;
		for (Checksum c : _checkSums) 
		{
			
			if (!c.check(ctx)) {
				LOG.warning(String.format("Checksum for source=%s, start=%d, end=%d, at=%d, does not check out", this.getId(), c.start, c.end, c.offset));
				return false;			
			}
		}
		return true;
	}
	
	public void updateChecksums(DDMIContext ctx) 
	{
		if (!isValid(ctx)) return;
		if (_checkSums == null) return;
		for (Checksum c : _checkSums) 
		{
			c.update(ctx);			
		}
	}
	
	public byte[] read(DDMIContext ctx, int off, int len)
	{
		
		if (_caches != null) 
		{
			for (Cache c : _caches) 
			{
				if (!c.appliesTo(off, len)) continue; 
				return c.read(ctx, off, len);
			}
		}
		for (int retry = 0; retry < MAX_RETRIES; retry++)
		{
			try {
				return readMedia(ctx, off, len);
			} catch (RuntimeException re) {
				if (retry == MAX_RETRIES - 1) throw re;
			}
		}
		throw new AssertionError("Unreachable code");
	}
	
	public void write(DDMIContext ctx, int off, byte[] data)
	{	

		
		if (_caches != null) 
		{
			for (Cache c : _caches) 
			{
				if (!c.appliesTo(off, data.length)) continue;
				// equalize writing action to multiples of 8
				c.write(ctx, off, data);
				return;
			}
		}
		

		for (int retry = 0; retry < MAX_RETRIES; retry++)
		{
			try {
				writeMedia(ctx, off, data);
			} catch (RuntimeException re) {
				if (retry == MAX_RETRIES - 1) throw re;
			}
		}
	}
	

	public void setValidIf(String condition) 
	{
		_condition = condition;		
	}
	
	public boolean isValid(DDMIContext ctx) 
	{
		if (ctx.getI2CLink() == null) return false;

		if (_condition == null) return true;

		Globals globals = ctx.getGlobals();
		
		LuaValue func = globals.load("return " + _condition);		
		LuaValue val =  func.call();
		boolean b=  val.checkboolean();
		return b;
	}

	
	abstract byte[] readMedia(DDMIContext ctx, int off, int len);
	
	abstract void writeMedia(DDMIContext ctx, int off, byte[] data);

	public void addCache(int start, int end) 
	{
		if (_caches == null) _caches = new ArrayList<Cache>();
		_caches.add(new Cache(start, end));
	} 
	
	
	public void clearCache(DDMIContext ctx) 
	{
		if (_caches == null) return;
		for (Cache c : _caches) {
			c.clear(ctx);
		}
	}

	public int size() {
		return end - start + 1;
	}

	public abstract String getPath();
}
