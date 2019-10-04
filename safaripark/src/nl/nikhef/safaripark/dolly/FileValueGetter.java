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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

import nl.nikhef.sfp.ddmi.DDMIValue;
import nl.nikhef.tools.Utils;

public class FileValueGetter implements ValueGetter {

	private Properties _props = new Properties();
	
	public FileValueGetter(File sourceFile) throws IOException {
		
		Reader r = new FileReader(sourceFile);
		try {
			_props.load(r);
		} finally {
			try {
				r.close();
			} catch (IOException e) {};
		}
	}

	@Override
	public byte[] getValue(DDMIValue v) {
		
		String name = v.getQualifiedName();
		
		String prop = _props.getProperty(name, null);
		
		if (prop == null) return null;
		
		return Utils.hexStringToBytes(prop);
	}

}
