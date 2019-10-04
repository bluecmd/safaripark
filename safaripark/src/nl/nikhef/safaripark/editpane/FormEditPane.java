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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;
import nl.nikhef.safaripark.Title;
import nl.nikhef.sfp.ddmi.DDMIElement;
import nl.nikhef.sfp.ddmi.DDMIGroup;
import nl.nikhef.sfp.ddmi.DDMIMeta;
import nl.nikhef.sfp.ddmi.DDMIValue;
import nl.nikhef.sfp.ddmi.DDMIValue.DDMIType;

@SuppressWarnings("serial")
public class FormEditPane extends JPanel {

	enum Direction {
		HORIZONTAL,
		VERTICAL
	}
	
	public FormEditPane(EditorContext ctx, DDMIGroup grp, int level) {
		
		setLayout(new MigLayout("wrap 4", "[sizegroup l, grow 0.3][sizegroup v,grow 0.7][sizegroup l,grow 0.3][sizegroup v,grow 0.7]"));
		addFields(ctx, grp, level);
	}
	
	private void addFields(EditorContext ctx, DDMIGroup grp, int level)
	{
		int col = 0;
		
		for (DDMIElement el : grp.getChildren())
		{
			
			if (el instanceof DDMIGroup) {
				DDMIGroup grp2 = DDMIGroup.class.cast(el);
				if (grp2.getLabel() == null) {
					addFields(ctx, grp2, level);
				} else {
					if (col % 2 == 1) {
						add(new JPanel(), "span 2");
						col += 1;
					}
					addGroup(ctx, DDMIGroup.class.cast(el), level);
				}
			} else if (el instanceof DDMIValue){
				if (DDMIValue.class.cast(el).getType() == DDMIType.PASSWORD_TYPE) continue;
				
				addField(ctx, DDMIValue.class.cast(el), level);
			}
			
			col += 1;
		}
	}

	private void addGroup(EditorContext ctx, DDMIGroup grp, int level) {
		
		JPanel titleBar = new JPanel();
		titleBar.setOpaque(true);
		titleBar.setBackground(UIManager.getColor("activeCaption"));
		titleBar.setLayout(new BoxLayout(titleBar,BoxLayout.LINE_AXIS));
		
		titleBar.add(new Title(grp.getLabel()));
		titleBar.add(Box.createGlue());

		add(titleBar, "span 4, growx");
		add(new FormEditPane(ctx, grp, level + 1), "span 4, growx");
		// Needed to align everything. Don't know why....
		add(new JPanel(), "span 2");
	}

	private void addField(EditorContext ctx, DDMIValue val, int level) {
		JLabel label = new JLabel(val.getShort());
		label.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 4));
		ValueEditor comp;
		switch (val.getType())
		{
		case INT_TYPE:
		case UINT_TYPE:
			if (DDMIMeta.LOOKUP.partOf(val)) {			
				comp = new EnumEditor(ctx, val, label);
			} else {
				comp = new IntegerEditor(ctx, val, label);
			}
			break;
		case TEXT_TYPE:
			comp = new TextEditor(ctx, val, label);
			break;
		case DECIMAL_TYPE_FLOAT:
		case DECIMAL_TYPE_SFIXED:
		case DECIMAL_TYPE_UFIXED:
			comp = new DecimalEditor(ctx, val, label);
			break;
		default:
			comp = new BitfieldEditor(ctx, val, label);
			break;
		
		}
		add(label, "right, aligny top");
		add(comp, "growx, aligny top");
	}

}
