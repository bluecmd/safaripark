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
import java.awt.ItemSelectable;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.UIManager;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

import nl.nikhef.tools.ListenerManager;

public class CheckableList<T> extends JList<T> implements ListDataListener, ItemSelectable  
{

	private Set<T> _checked = new HashSet<T>();
	private CheckboxCellRenderer<T> _renderer;
	private ListenerManager<ItemListener> _lm = new ListenerManager<ItemListener>(ItemListener.class);
	
	public CheckableList() {
		initComponent();
	}
	
	public CheckableList(T[] items) {
		super(items);
		initComponent();
	}
	
	
	public CheckableList(ListModel<T> dataModel) {
		super(dataModel);
		dataModel.addListDataListener(this);
		initComponent();
	}

	private void emitItemChange(T obj, boolean selected) {
		ItemEvent ie = new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, 
				obj, selected ? ItemEvent.SELECTED : ItemEvent.DESELECTED);
		
		_lm.getProxy().itemStateChanged(ie);
	}
	
	private void initComponent() {
		addMouseListener(new MouseAdapter() {
		    public void mousePressed(MouseEvent e) {
				int index = locationToIndex(e.getPoint());
				if (index != -1 && e.getPoint().getX() < _renderer.checkboxWidth) 
				{
					T obj = getModel().getElementAt(index);
					if (_checked.contains(obj)) {
						_checked.remove(obj);
						emitItemChange(obj, false);
					} else {
						_checked.add(obj);
						emitItemChange(obj, true);
					}
					repaint();
					
					
				}
		    }
		});
		
		_renderer = new CheckboxCellRenderer();
		setCellRenderer(_renderer);
	}

	public Collection<T> getSelectedItems() 
	{
		return Collections.unmodifiableCollection(_checked);
	}
	
	protected class CheckboxCellRenderer<T> extends JCheckBox implements ListCellRenderer<T> 
	{

		protected final int checkboxWidth;
		
		public CheckboxCellRenderer() {
			setOpaque(false);
			checkboxWidth =  UIManager.getIcon("CheckBox.icon").getIconWidth() + 8;	// So, this is a bit of a nasty hack, the +8 ... should be twice the margin			
		}
		
		
		
		@Override
		public Component getListCellRendererComponent(JList<? extends T> list, T value, int index,
				boolean isSelected, boolean cellHasFocus) {
			
			setText(value.toString());
			
			if (_checked.contains(value)) {
				this.setSelected(true);
			} else {
				this.setSelected(false);
			}
			
			return this;
		}
	}

	private boolean contains(T object) {
		
		// fast way
		if (getModel() instanceof DefaultListModel) 
		{
			return DefaultListModel.class.cast(getModel()).contains(object);
		}
		
		// backup slow way
		for (int i = 0; i < getModel().getSize(); ++i) 
		{
			if (getModel().getElementAt(i).equals(object)) return true;
		}
		return false;
	}
	
	/**
	 * Update checked must be called if the contents of the list changes.
	 * When providing a model, this function will be called automatically if the model changes.
	 */
	public void updateChecked() 
	{
		Iterator<T> e = _checked.iterator();
		
		while (e.hasNext()) {
			T obj = e.next();
			if (!contains(obj)) 
			{
				e.remove();
				emitItemChange(obj, false);
			}
		}
	}
	
	@Override
	public void intervalAdded(ListDataEvent e) {
		updateChecked();
	}

	@Override
	public void intervalRemoved(ListDataEvent e) {
		updateChecked();
	}

	@Override
	public void contentsChanged(ListDataEvent e) {
		updateChecked();
	}

	@Override
	public Object[] getSelectedObjects() {
		return _checked.toArray(new Object[_checked.size()]);
	}

	@Override
	public void addItemListener(ItemListener l) {
		_lm.addListener(l);;
	}

	@Override
	public void removeItemListener(ItemListener l) {
		_lm.removeListener(l);
	}
	
	
}
