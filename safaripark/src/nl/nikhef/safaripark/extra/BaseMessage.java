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
package nl.nikhef.safaripark.extra;

public class BaseMessage implements Message {

	private final boolean _hasOperation;
	private final String _messageText;
	private final String _operationText;
	
	public BaseMessage(String messageText) {
		_hasOperation = false;
		_messageText = messageText;
		_operationText = null;
	}
	
	public BaseMessage(String messageText, String operationText) {
		_hasOperation = true;
		_messageText = messageText;
		_operationText = operationText;
	}
	
	@Override
	public String getMessage() {
		return _messageText;
	}

	@Override
	public boolean hasOperation() {
		return _hasOperation;
	}

	@Override
	public String getOperationText() {
		return _operationText;
	}

	@Override
	public void runOperation() {

	}

	@Override
	public void repelOperation() {

	}

}
