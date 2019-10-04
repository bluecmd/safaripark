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
package nl.nikhef.safaripark.vsp;

import javax.swing.tree.DefaultMutableTreeNode;

import nl.nikhef.sfp.ddmi.DDMIElement;

/**
 * Checkable tree node allows you to check your tree items.
 * 
 * It works in two modes: Either it is in branch mode, in which its
 * checked state is completely dependent on the child nodes, or it is in
 * leaf mode, in which its state is either checked or unchecked.
 *  
 * @author vincentb
 */
public class CheckableTreeNode extends DefaultMutableTreeNode 
{

	private final String _label;
	private boolean _checked;
	private CheckableTreeNode _parent;
	
	public CheckableTreeNode(CheckableTreeNode parent, DDMIElement userObject, String label) {
		super(userObject);
		_label = label;
		_parent = parent;
	}

	public void check()
	{
		_checked = true;
	}
	
	public void uncheck() 
	{
		_checked = false;
	}
	
	protected void checkStateChanged() {
		if (_parent != null) {
			_parent.checkStateChanged();
		}
	}
	
	
	@Override
	public void setUserObject(Object userObject) {
		if (userObject instanceof Integer) {
			// OMG.. this is ugly!             (but it works...)
			
			int v = Integer.class.cast(userObject).intValue();
			
			if (isLeaf()) {
				if (v == TristateCheckbox.CHECKED) {
					check();
				} else {
					uncheck();
				}
			} else {
				setChildState(v == TristateCheckbox.CHECKED);
			}
			checkStateChanged();
			
			return;
		}
		super.setUserObject(userObject);
	}

	
	
	private void setChildState(boolean checked) {
		for (int i = 0; i < getChildCount(); i++) {
			CheckableTreeNode ctn = CheckableTreeNode.class.cast(getChildAt(i));
			if (ctn.isLeaf()) {
				ctn._checked = checked;
			} else {
				ctn.setChildState(checked);
			}
		}
	}

	public int checkChildren() {
		int mask = 0;
		for (int i = 0; i < getChildCount(); i++) {
			CheckableTreeNode ctn = CheckableTreeNode.class.cast(getChildAt(i));
			mask |= 1 << (ctn.getState());
		}
		return mask;
	}
	
	public int getState()
	{
		if (isLeaf()) {
			return _checked ? TristateCheckbox.CHECKED : TristateCheckbox.UNCHECKED;
		} else {
			switch (checkChildren()) {
			case 0x1: return TristateCheckbox.UNCHECKED;
			case 0x4: return TristateCheckbox.CHECKED;
			default:  return TristateCheckbox.INDETERMINATE;
			}
		}
		
	}
	
	public boolean isChecked() {
		return _checked;
	}

	public String getLabel() {
		return _label;
	}
	
}
