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

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.util.Arrays;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.event.ChangeEvent;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreePath;

public class CheckBoxNodeEditor extends AbstractCellEditor implements TreeCellEditor {

	CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();

	ChangeEvent changeEvent = null;

	JTree tree;
	boolean firstTime = true;

	public CheckBoxNodeEditor(JTree tree) {
		this.tree = tree;
		
	}


	public boolean isCellEditable(EventObject event) {
		if (event instanceof MouseEvent) {
			MouseEvent mouseEvent = (MouseEvent) event;
			TreePath path = tree.getPathForLocation(mouseEvent.getX(),
					mouseEvent.getY());
			if (path != null) {
				Object node = path.getLastPathComponent();
				if ((node != null) && (node instanceof CheckableTreeNode)) {
					// CheckBoxNodeEditor treeNode = CheckBoxNodeEditor.class.cast(node);
					return true;
				}
			}
		}
		return false;
	}

	public Component getTreeCellEditorComponent(final JTree tree, Object value,
			boolean selected, boolean expanded, boolean leaf, int row) {

		Component editor = renderer.getTreeCellRendererComponent(tree, value,
				true, expanded, leaf, row, true);

		// editor always selected / focused
		ItemListener itemListener = new ItemListener() {
			public void itemStateChanged(ItemEvent itemEvent) {
				if (stopCellEditing()) {
					fireEditingStopped();
					tree.repaint();
				}
			}
		};
		if (editor instanceof JCheckBox && firstTime) {
//			JCheckBox cb = JCheckBox.class.cast(editor);
			
			((JCheckBox) editor).addItemListener(itemListener);
			firstTime = false;
		}

		return editor;
	}


	@Override
	public Object getCellEditorValue() {
		TristateCheckbox editor = renderer.getRenderer();
		return editor.getSelectionState();
	}
}
