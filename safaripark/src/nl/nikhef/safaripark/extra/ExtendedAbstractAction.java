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

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;

@SuppressWarnings("serial")
public abstract class ExtendedAbstractAction extends AbstractAction {

	public ExtendedAbstractAction(String name, Icon icon, String shortDescription) {
		super(name, icon);
		putValue(Action.SHORT_DESCRIPTION, shortDescription);
	}

	
}