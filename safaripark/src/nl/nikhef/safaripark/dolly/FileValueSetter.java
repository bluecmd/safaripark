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
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Properties;

import nl.nikhef.sfp.ddmi.DDMIValue;
import nl.nikhef.tools.Utils;

public class FileValueSetter implements ValueSetter {

	private Properties _props;
	private File       _target;
	
	public FileValueSetter(File targetFile) 
	{
		_props = new Properties();
		_target = targetFile;
	}

	@Override
	public void setValue(DDMIValue value, byte[] data) throws IOException 
	{
		String qName = value.getQualifiedName();
		_props.setProperty(qName, Utils.bytesToHexString(data));
	}

	@Override
	public void finished() throws IOException {
		Writer w = new FileWriter(_target);
		try {
			_props.store(w, "SFP Module Values file");
		} finally {
			try {
				w.close();
			} catch (IOException e) { }	// prevent close exception from overwriting the actual error
		}
	}

}
