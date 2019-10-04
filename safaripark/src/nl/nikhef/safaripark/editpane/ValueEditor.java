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
package nl.nikhef.safaripark.editpane;

import java.awt.Font;
import java.util.Arrays;

import javax.swing.JLabel;
import javax.swing.JPanel;

import nl.nikhef.sfp.ddmi.AccessException;
import nl.nikhef.sfp.ddmi.DDMIMeta;
import nl.nikhef.sfp.ddmi.DDMIUtils;
import nl.nikhef.sfp.ddmi.DDMIValue;
import nl.nikhef.tools.Utils;

@SuppressWarnings("serial")
public abstract class ValueEditor extends JPanel {

	protected final DDMIValue value;

	protected byte[]	rawValue;
	protected boolean   dirty;
	private  JLabel     _label;
	private EditorContext  _ctx;
	
	public ValueEditor(EditorContext ctx, DDMIValue value, JLabel label) 
	{
		this.value = value;
		_ctx = ctx;
		_label = label;
		ctx.addEditor(this);
	}
	
	
	/**
	 * Sub-component must update the value.
	 */
	protected abstract void updateValue();

	/**
	 * Sub-component must clear the value, and disable the editor.
	 */
	protected abstract void clearValue();

	
	public boolean isValueValid() {
		if (_ctx == null) return false;
		if (_ctx.getContext() == null) return false;
		return value.isValid(_ctx.getContext());
	}
	
	public void updateEditor() {
		
		dirty = false;
		
		updateLabel();
		if (_ctx.getContext() == null || !isValueValid()) 
		{
			rawValue = null;
			setEnabled(false);
			clearValue();
		} else {
			
			rawValue = value.readRaw(_ctx.getContext());
			if (rawValue == null) 
			{
				setEnabled(false);
				clearValue();
			} else {
				setEnabled(true);
				updateValue();
			}
			
			
		}
		
		
		
	}
	
	private void updateLabel() 
	{
		if (dirty) {
			_label.setFont(_label.getFont().deriveFont(Font.BOLD | Font.ITALIC));
		} else {
			_label.setFont(_label.getFont().deriveFont(Font.PLAIN));
		}
	}
	
	
	public void setRawValue(byte[] newRawValue) 
	{
		rawValue = newRawValue;
		if (Arrays.equals(rawValue, value.readRaw(_ctx.getContext()))) {
			dirty = false;			
		} else {
			dirty = true;
		}
		
		updateLabel();
		_ctx.changeDirtyState(this);
	}

	

	public String getAsString() 
	{
		if (rawValue == null) return "";
		return DDMIUtils.rawToString(value, rawValue);
	}
	
	public void setAsString(String str)
	{
		setRawValue(DDMIUtils.stringToRaw(value, str));
	}
	
	public int getAsInt() 
	{
		if (rawValue == null) return 0;
		
		return DDMIUtils.rawToInt(rawValue, value);
	}
	
	public void setAsInt(int v) 
	{
		setRawValue(DDMIUtils.intToRaw(value, v));
	}
	
	public void setAsDecimal(float v) 
	{
		setRawValue(DDMIUtils.decimalToRaw(value, v));
	}
	


	public float getAsDecimal() {
		return DDMIUtils.rawToDecimal(value, rawValue);
	}

	
	public void commit() 
	{
		
		if (!dirty) return;
		
		value.writeRaw(_ctx.getContext(), rawValue);
		dirty = false;
		updateLabel();
		_ctx.changeDirtyState(this);
	}


	public void revert() 
	{
		if (!dirty) return;
		updateEditor();
		_ctx.changeDirtyState(this);
	}

	
}
