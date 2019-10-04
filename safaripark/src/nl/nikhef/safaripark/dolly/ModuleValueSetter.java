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
package nl.nikhef.safaripark.dolly;

import java.io.IOException;

import nl.nikhef.safaripark.ContextCache;
import nl.nikhef.safaripark.extra.Module;
import nl.nikhef.sfp.ddmi.DDMI;
import nl.nikhef.sfp.ddmi.DDMIContext;
import nl.nikhef.sfp.ddmi.DDMIValue;
import nl.nikhef.sfp.ddmi.DataSource;

public class ModuleValueSetter implements ValueSetter {

	private Module _mod;
	private ContextCache _ctxCache;
	
	public ModuleValueSetter(ContextCache cache, Module targetModule) {
		_mod = targetModule;
		_ctxCache = cache;
	}

	@Override
	public void setValue(DDMIValue value, byte[] data) throws IOException {
		value.writeRaw(_ctxCache.getContextFor(_mod.dev, _mod.bay), data);
	}

	@Override
	public void finished() throws IOException {
		DDMIContext ctx = _ctxCache.getContextFor(_mod.dev, _mod.bay);
		DDMI ddmi = ctx.getDDMI();
		for (DataSource ds: ddmi.getSources()) {
			ds.updateChecksums(ctx);
		}
	}

}
