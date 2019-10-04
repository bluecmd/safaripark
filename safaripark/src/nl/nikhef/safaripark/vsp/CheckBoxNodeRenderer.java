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
import java.awt.Font;

import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.TreeCellRenderer;

public class CheckBoxNodeRenderer implements TreeCellRenderer 
{
	private TristateCheckbox renderer = new TristateCheckbox();

	Color selectionBorderColor, selectionForeground, selectionBackground,
	textForeground, textBackground;


	public CheckBoxNodeRenderer() {
		Font fontValue;
		fontValue = UIManager.getFont("Tree.font");
		if (fontValue != null) {
			renderer.setFont(fontValue);
		}
		Boolean booleanValue = (Boolean) UIManager
				.get("Tree.drawsFocusBorderAroundIcon");
		renderer.setFocusPainted((booleanValue != null)
				&& (booleanValue.booleanValue()));

		selectionBorderColor = UIManager.getColor("Tree.selectionBorderColor");
		selectionForeground = UIManager.getColor("Tree.selectionForeground");
		selectionBackground = UIManager.getColor("Tree.selectionBackground");
		textForeground = UIManager.getColor("Tree.textForeground");
		textBackground = UIManager.getColor("Tree.textBackground");
	}
	
	
	public Component getTreeCellRendererComponent(JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row,
			boolean hasFocus) {

		renderer.setSelected(false);

		renderer.setEnabled(tree.isEnabled());

		if (selected) {
			renderer.setForeground(selectionForeground);
			renderer.setBackground(selectionBackground);
		} else {
			renderer.setForeground(textForeground);
			renderer.setBackground(textBackground);
		}

		if ((value != null) && (value instanceof CheckableTreeNode)) {
/*				Object userObject = ((CheckableTreeNode) value)
						.getUserObject();*/
			
			CheckableTreeNode ctn = CheckableTreeNode.class.cast(value);
			renderer.setSelectionState(ctn.getState());
			renderer.setText(ctn.getLabel());
		} else {
			String stringValue = tree.convertValueToText(value, selected,
					expanded, leaf, row, false);
			renderer.setText(stringValue);
		}
		return renderer;
	}


	public TristateCheckbox getRenderer() {
		return renderer;
	}
}
