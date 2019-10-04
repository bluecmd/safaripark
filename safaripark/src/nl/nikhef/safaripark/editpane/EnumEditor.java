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

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Map;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicComboBoxEditor;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

import nl.nikhef.sfp.ddmi.DDMIMeta;
import nl.nikhef.sfp.ddmi.DDMIMeta.LookupTable;
import nl.nikhef.sfp.ddmi.DDMIValue;

@SuppressWarnings("serial")
public class EnumEditor extends ValueEditor implements ActionListener {


	private JComboBox<Integer> _cb;
	private LookupTable _lut;
	private boolean		_ignoreEvents = false;
	
	private class NumberComboxEditor extends BasicComboBoxEditor 
	{
		private JFormattedTextField.AbstractFormatter _editFormatter = null;
				 

		private JFormattedTextField.AbstractFormatter _displayFormatter = null;

		private Integer consolidate(Object obj)
		{
			
			if (obj == null) {
				return Integer.valueOf(0);
			}
			
			if (obj instanceof Number) {
				return Number.class.cast(obj).intValue();
			}
			throw 
			new RuntimeException(String.format("Could not convert object %s of type %s into an integer", 
					obj, obj.getClass().getSimpleName()));
		}
		
		@Override
		protected JTextField createEditorComponent() {
			if (_editFormatter == null) _editFormatter = new NumberFormatter(NumberFormat.getIntegerInstance());
			if (_displayFormatter == null) {
				_displayFormatter = new JFormattedTextField.AbstractFormatter() {
					
					@Override
					public String valueToString(Object value) throws ParseException {
						
						Integer i = consolidate(value);
						if (_lut.containsKey(i)) {
							return String.format("%d (%s)", i, _lut.get(i));
						} else {
							return i.toString();	
						}
						
						
					}
					
					@Override
					public Object stringToValue(String text) throws ParseException {
						return null;
					}
				};

			}
			
			JFormattedTextField tft = new JFormattedTextField(
					new DefaultFormatterFactory(_displayFormatter, _displayFormatter, _editFormatter)
				);
			
			return tft;
		}
		
		
	}
	
	private class LookupRenderer extends DefaultListCellRenderer
	{
		
		
		

		@Override
		public Component getListCellRendererComponent(JList<? extends Object> list, Object value, int index,
				boolean isSelected, boolean cellHasFocus) {
			
			Component cp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
			
			Integer i = Integer.class.cast(value);
			
			if (cp instanceof JLabel) {
				if (_lut.containsKey(i)) {
					((JLabel) cp).setText(String.format("%d (%s)", i, _lut.get(i)));
				} else {
					((JLabel) cp).setText(Integer.toString(i));
				}
			}
			
			return cp;
		}
		
	}
	
	public EnumEditor(EditorContext ctx, DDMIValue value, JLabel label) 
	{
		super(ctx, value, label);
		setLayout(new BorderLayout());
		_lut = DDMIMeta.LOOKUP.of(value);
		
		_cb = new JComboBox<Integer>();
		_cb.setEditable(value.isWritable());
		_cb.setEditor(new NumberComboxEditor());
		_cb.setRenderer( new LookupRenderer());
	
	
		
		for (Map.Entry<Integer, String> e : _lut.entrySet())
		{
			_cb.addItem(e.getKey());				
		}
		
		
		add(_cb, BorderLayout.CENTER);
		
		clearValue();
		
		_cb.addActionListener(this);
		
	}
	
	

	@Override
	protected void updateValue() 
	{
		_ignoreEvents = true;
		_cb.setEnabled(isValueValid());
		Integer val = Integer.valueOf(getAsInt());
		_cb.setSelectedItem(val);
		_cb.getEditor().setItem(val);
		 JFormattedTextField.class.cast(_cb.getEditor().getEditorComponent()).setValue(val);
		_ignoreEvents = false;
	}


	@Override
	protected void clearValue() {
		_ignoreEvents = true;
		_cb.setEnabled(false);
		_cb.setSelectedItem(0);
		_cb.getEditor().setItem(0);
		JFormattedTextField.class.cast(_cb.getEditor().getEditorComponent()).setValue(0);
		_ignoreEvents = false;
	}
	

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		if (_ignoreEvents) return;
		Object obj = _cb.getEditor().getItem();
		if (obj instanceof Integer) {
			setAsInt(Integer.class.cast(obj).intValue());
			updateValue();
		}
	}

	
	
}
