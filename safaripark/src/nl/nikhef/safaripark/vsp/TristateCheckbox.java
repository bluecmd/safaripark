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

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.UIManager;

public class TristateCheckbox extends JCheckBox implements Icon, ActionListener, ItemListener {

	final static boolean MIDasSELECTED = true;  //consider mid-state as selected ?

	
	public static final int UNCHECKED = 0;
	public static final int INDETERMINATE = 1;
	public static final int CHECKED = 2;
	
	private ArrayList<ItemListener> _listeners = new ArrayList<ItemListener>();
	
	private boolean _isIndeterminate = false;;
	private boolean _ignoreEvents = false;

	public TristateCheckbox() { this(""); }

	public TristateCheckbox(String text) {
		super(text);
		setIcon(this);
		
		//addActionListener(this);
		super.addItemListener(this);
	}

	public TristateCheckbox(String text, int sel) {
		/* tri-state checkbox has 3 selection states:
		 * 0 unselected
		 * 1 mid-state selection
		 * 2 fully selected
		 */
		super(text, sel == CHECKED);

		super.addItemListener(this);
		setIcon(this);
	}


	public int getSelectionState() {
		if (_isIndeterminate)
		{
			return INDETERMINATE;
		} else if (isSelected()) 
		{
			return CHECKED; 
		}
		return UNCHECKED;
	}

	public void setSelectionState(int sel) {
		_ignoreEvents = true;
		switch (sel) {
		case CHECKED:
			setSelected(true);
			_isIndeterminate = false;
			break;
		case INDETERMINATE:
			setSelected(false);
			_isIndeterminate = true;
			break;
		case UNCHECKED:
			_isIndeterminate = false;
			setSelected(false);
			break;
		}
		_ignoreEvents = false;
	}


	final static Icon icon = UIManager.getIcon("CheckBox.icon");

	@Override
	public void paintIcon(Component c, Graphics g, int x, int y) {
		icon.paintIcon(c, g, x, y);
		if (getSelectionState() != INDETERMINATE) return;

		int w = getIconWidth();
		int h = getIconHeight();
		g.setColor(c.isEnabled() ? new Color(51, 51, 51) : new Color(122, 138, 153));
		g.fillRect(x+4, y+4, w-8, h-8);

		if (!c.isEnabled()) return;
		g.setColor(new Color(81, 81, 81));
		g.drawRect(x+4, y+4, w-9, h-9);
	}

	@Override
	public int getIconWidth() {
		return icon.getIconWidth();
	}

	@Override
	public int getIconHeight() {
		return icon.getIconHeight();
	}

	public void actionPerformed(ActionEvent e) {
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		
		_isIndeterminate = false;
		
		// test
		for (ItemListener l : _listeners)
		{
			l.itemStateChanged(e);
		}
		_ignoreEvents = false;
	}
	
	@Override
	public void addItemListener(ItemListener l) {
		_listeners.add(l);
	}
}
