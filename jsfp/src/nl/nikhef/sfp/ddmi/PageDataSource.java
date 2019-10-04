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

import java.util.logging.Logger;

import nl.nikhef.sfp.SFPManager;
import nl.nikhef.tools.Utils;

public class PageDataSource extends DataSource {

	private static final Logger LOG = Logger.getLogger(PageDataSource.class.getSimpleName());
	
	
	private final DataSource _parent;
	private final int        _pageNo;
	
	public PageDataSource(DataSource parent, int pageNo, int start, int end) 
	{
		super(start, end);
		assert(parent != null);
		_parent = parent;
		_pageNo = pageNo;
		
	}
	
	public boolean isValid(DDMIContext ctx) 
	{
		return _parent.isValid(ctx);
	}

	
	
	private void selectPage(DDMIContext ctx) 
	{
		LOG.fine("Selecting page " + _pageNo);
		if (_parent.getPageSelect() == -1)
			throw new RuntimeException("Parent has no page select!");
		
		byte[] pageData = new byte[] { (byte)_pageNo };
		
		_parent.writeMedia(ctx, _parent.getPageSelect(), pageData);
		
	}
	
	@Override
	byte[] readMedia(DDMIContext ctx, int off, int len) 
	{
		selectPage(ctx);
		byte[] dta = _parent.readMedia(ctx, off, len);
		// Utils.dumpHex("Page: " + _pageNo + ", Reading " + off + ":" + len, dta);
		return dta;
	}

	@Override
	void writeMedia(DDMIContext ctx, int off, byte[] data) 
	{
		selectPage(ctx);
		// Utils.dumpHex("Page: " + _pageNo + ", Writign " + off + ":" + data.length, data);
		_parent.writeMedia(ctx, off, data);
	}

	
	@Override
	public String getPath() {
		return _parent.getPath() + "_page" + _pageNo;
	}
}
