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
package nl.nikhef.safaripark;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.UIManager;

@SuppressWarnings("serial")
public class Title extends JLabel {

	public Title(String name) 
	{
		super(name);
		setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
		setOpaque(true);
		setBackground(UIManager.getColor("activeCaption"));
		setForeground(UIManager.getColor("activeCaptionText"));
		
	}

	public Title(String name, Icon icon) {
		this(name);
		setIcon(icon);
	}
	
}
