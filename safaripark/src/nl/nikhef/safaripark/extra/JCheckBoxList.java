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

import java.awt.Component;

import javax.swing.*;
import javax.swing.border.*;

import java.awt.event.*;

@SuppressWarnings("serial")
public class JCheckBoxList extends JList<JCheckBox> {
  protected static Border noFocusBorder = new EmptyBorder(1, 1, 1, 1);

  public JCheckBoxList() {
    setCellRenderer(new CellRenderer());
    addMouseListener(new MouseAdapter() {
      public void mousePressed(MouseEvent e) {
        int index = locationToIndex(e.getPoint());
        if (index != -1) {
          JCheckBox checkbox = (JCheckBox) getModel().getElementAt(index);
          checkbox.setSelected(!checkbox.isSelected());
          repaint();
        }
      }
    });
    setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
  }

  public JCheckBoxList(ListModel<JCheckBox> model){
    this();
    setModel(model);
  }

  protected class CellRenderer implements ListCellRenderer<JCheckBox> {
    public Component getListCellRendererComponent(
        JList<? extends JCheckBox> list, JCheckBox value, int index,
        boolean isSelected, boolean cellHasFocus) {
      JCheckBox checkbox = value;

      //Drawing checkbox, change the appearance here
      checkbox.setBackground(isSelected ? getSelectionBackground()
          : getBackground());
      checkbox.setForeground(isSelected ? getSelectionForeground()
          : getForeground());
      checkbox.setEnabled(isEnabled());
      checkbox.setFont(getFont());
      checkbox.setFocusPainted(false);
      checkbox.setBorderPainted(true);
      checkbox.setBorder(isSelected ? UIManager
          .getBorder("List.focusCellHighlightBorder") : noFocusBorder);
      return checkbox;
    }
  }
}
