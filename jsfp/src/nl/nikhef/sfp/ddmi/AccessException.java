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

@SuppressWarnings("serial")
public class AccessException extends RuntimeException {

	public AccessException() {
	}

	public AccessException(String arg0) {
		super(arg0);
	}

	public AccessException(Throwable arg0) {
		super(arg0);
	}

	public AccessException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public AccessException(String arg0, Throwable arg1, boolean arg2,
			boolean arg3) {
		super(arg0, arg1, arg2, arg3);
	}

}
