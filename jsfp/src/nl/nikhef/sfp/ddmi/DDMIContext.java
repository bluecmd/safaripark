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

import java.util.HashMap;
import java.util.Map;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.TwoArgFunction;
import org.luaj.vm2.lib.jse.JsePlatform;

import nl.nikhef.sfp.i2c.I2CLink;

public class DDMIContext 
{
	
	private Globals _globals;
	private I2CLink _i2cLink;
	private DDMI    _ddmi;
	
	public final Map<String, Object> scratch = new HashMap<String, Object>(); 

	private class ElementTable extends LuaValue 
	{

		@Override
		public int type() {
			return LuaValue.TTABLE;
		}

		@Override
		public String typename() {
			return "table";
		}
		
		public boolean istable()             { return true; }
		
		@Override
		public LuaValue rawget(LuaValue obj) {
			return rawget(obj.toString());
		}
		@Override
		public LuaValue rawget(String key) {
			
			DDMIElement element = _ddmi.getElementById(key);
			
			if (element instanceof DDMIValue) {
				DDMIValue value = DDMIValue.class.cast(element);
				switch (value.getType()) {
				case INT_TYPE:
				case BITMAP_TYPE:
					return LuaValue.valueOf(DDMIUtils.readInt(value, DDMIContext.this));
				case TEXT_TYPE:
					return LuaValue.valueOf(DDMIUtils.readString(value, DDMIContext.this));
				default:
					break;
				}
			}
			
			return LuaValue.NIL;
		}
	}
	
	public DDMIContext(I2CLink link, DDMI ddmi) {
		_i2cLink = link;
		_ddmi = ddmi;
		_globals = JsePlatform.standardGlobals();
		_globals.set("isset",  new TwoArgFunction() {

			@Override
			public LuaValue call(LuaValue value, LuaValue bit) {
				LuaValue val = LuaValue.valueOf((value.toint() & (1 << bit.toint())) != 0);
				return val;
				
			}
			
		});
		
		_globals.set("id", new ElementTable());
	}
	
	
	public DDMI getDDMI() {
		return _ddmi;
	}


	public void updateChecksums() {
		_ddmi.updateChecksums(this);
	}
	
	public boolean verifyChecksums() {
		return _ddmi.verifyChecksums(this);
	}

	
	
	public Globals getGlobals() {
		return _globals;
	}


	public I2CLink getI2CLink() {
		return _i2cLink;
	}
	


	
}
