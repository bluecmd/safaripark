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
package nl.nikhef.rebus.ftdi;

import nl.nikhef.tools.Utils;

public class MPSSECmdBuilder {
	
	private byte[] _buffer = new byte[127];
	private int _ptr = 0;
	
	
	private void ensure(int length) 
	{
		// first check if it fits. If it does, never mind
		if (_ptr + length <= _buffer.length) return;
		
		// make the new array, twice as big as the previous
		byte[] newBuffer = new byte[_buffer.length * 2];
		
		// copy data into it
		System.arraycopy(_buffer, 0, newBuffer, 0, _ptr);
		
		// make the new buffer the current buffer
		_buffer = newBuffer;
	}
	
	private void append(int data) {
		_buffer[_ptr++] = (byte)data;
	}
	
	public void cmd(int cmd, int ... data)
	{
		ensure(1 + data.length);
		append(cmd);
		for (int i = 0; i < data.length; i++) {
			append(data[i]);
		}
	}

	public byte[] getBytes() {
		byte[] out = new byte[_ptr];
		System.arraycopy(_buffer, 0, out, 0, out.length);
		
		// Utils.dumpHex("Bytes out: ", out);
		
		return out;
	}

	public void reset() {
		_ptr = 0;		
	}
	
	

}
