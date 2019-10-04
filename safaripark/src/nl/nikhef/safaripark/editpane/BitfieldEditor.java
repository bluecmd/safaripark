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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;

import nl.nikhef.sfp.ddmi.DDMIMeta;
import nl.nikhef.sfp.ddmi.DDMIMeta.BitFieldValue;
import nl.nikhef.sfp.ddmi.DDMIValue;
import nl.nikhef.tools.Bits;

@SuppressWarnings("serial")
public class BitfieldEditor extends ValueEditor implements ActionListener, DocumentListener 
{
	private JFormattedTextField _tf;
	private MaskFormatter       _mf;
	private boolean				_ignoreEvents = true;
	
	private Map<DDMIMeta.BitFieldValue, JComponent> _b2c = null;
	
	public BitfieldEditor(EditorContext ctx, DDMIValue value, JLabel label) 
	{
		super(ctx, value, label);
		_tf = new JFormattedTextField();
		StringBuilder sb = new StringBuilder("HH");
		for (int i = 1; i < value.getLength(); ++i)
		{
			sb.append("-HH");
		}
		
		try {
			_mf = new MaskFormatter(sb.toString());
		} catch (ParseException e) {
			throw new RuntimeException("Unexpected! Mask error", e);
		}
		_mf.setPlaceholderCharacter('_');
		_tf.setFormatterFactory(new DefaultFormatterFactory(_mf));
		setLayout(new BorderLayout(0, 0));
		add(_tf, BorderLayout.CENTER);
		
		_tf.addActionListener(this);
		_tf.getDocument().addDocumentListener(this);
		
		
		if (DDMIMeta.BITFIELD.partOf(value)) {
			_b2c = new HashMap<DDMIMeta.BitFieldValue, JComponent>();
			DDMIMeta.BitField bf = DDMIMeta.BITFIELD.of(value);
			JPanel jp = new JPanel();
			jp.setLayout(new GridLayout(bf.fields().size(), 1));
			
			for (DDMIMeta.BitFieldValue bfv : bf.fields())
			{
				if (bfv.length == 1) {
					JCheckBox jc = new JCheckBox(bfv.name);
					jc.setEnabled(false);
					jc.addActionListener(this);
					
					jp.add(jc);
					_b2c.put(bfv, jc);					
				}
			}
			add(jp, BorderLayout.SOUTH);
		}
		
		clearValue();
	}
	
	
	
	private String toHex(byte b) 
	{
		return String.format("%02x", b);
	}

	protected void clearValue()
	{
		_tf.setEnabled(false);
		_tf.setEditable(false);
		updateComponent();
	}
	
	protected void updateValue()
	{	
		_tf.setEnabled(isValueValid());
		_tf.setEditable(value.isWritable());
		updateComponent();
	}
	
	private void updateBitmap() {
		if (_b2c == null) return;
		_ignoreEvents = true;
		for (Map.Entry<DDMIMeta.BitFieldValue, JComponent> e : _b2c.entrySet()) 
		{
			JComponent comp = e.getValue();
			BitFieldValue bfv = e.getKey();
			
			comp.setEnabled(isValueValid() && value.isWritable());
			if (bfv.length == 1) {
				boolean bitV;
				if (rawValue != null) {
					 bitV = Bits.get(rawValue, bfv.offset);
				} else {
					bitV = false;
				}
				JCheckBox cb = JCheckBox.class.cast(comp);
				if (cb.isSelected() != bitV) cb.setSelected(bitV);
			}
		}
		_ignoreEvents = false;
	}
	
	private void updateComponent() {
		
		_ignoreEvents = true;

		if (isValueValid() && rawValue != null) {
			StringBuilder sb = new StringBuilder(toHex(rawValue[0]));
			for (int i = 1; i < rawValue.length; i++)
			{
				sb.append('-');
				sb.append(toHex(rawValue[i]));			
			}
	
			
			_tf.setText(sb.toString());
		} else {
			_tf.setText("");
		}
		
		_ignoreEvents = false;
		
		updateBitmap();
	}



	@Override
	public void actionPerformed(ActionEvent ae) 
	{
		if (_ignoreEvents) return;
		
		byte[] copy = Arrays.copyOf(rawValue, rawValue.length);
		
		if ( (ae.getSource() instanceof JCheckBox)) {
		
			// look up bit value
			for (Map.Entry<DDMIMeta.BitFieldValue, JComponent> e : _b2c.entrySet()) 
			{
				JComponent comp = e.getValue();
				BitFieldValue bfv = e.getKey();
				
				if (comp != ae.getSource()) continue;
					
				// found it!
				
				Bits.update(copy, bfv.offset, JCheckBox.class.cast(comp).isSelected()); 			
				break;
			}
		}
		
		setRawValue(copy);
		
		updateComponent();
		
				
	}


	private int hexCharToVal(char c) {
		if (c >= '0' && c <= '9') return (int)(c - '0');
		if (c >= 'A' && c <= 'F') return (int)(c - 'A') + 10;
		if (c >= 'a' && c <= 'f') return (int)(c - 'a') + 10;
		return -1;
	}
	
	private void valueUpdated() {
		if (_ignoreEvents) return;
		
		byte[] copy = Arrays.copyOf(rawValue, rawValue.length);
		
		String val = _tf.getText();
		for (int i = 0; i < copy.length; i++) {
			int c1p = i * 3;
			char c1, c2;
			int c2p = i * 3 + 1;
			if (c1p < val.length()) c1 = val.charAt(c1p);
				else c1 = '0';
			if (c1 == '_') c1 = '0';
			 
			if (c2p < val.length()) c2 = val.charAt(c2p);
				else c2 = '0';
			if (c2 == '_') c2 = '0';			
			
			copy[i] = (byte)((hexCharToVal(c1) << 4) | hexCharToVal(c2));
			
		}
		setRawValue(copy);		
		updateBitmap();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		valueUpdated();
	}



	@Override
	public void removeUpdate(DocumentEvent e) {
		valueUpdated();
	}



	@Override
	public void changedUpdate(DocumentEvent e) {
		valueUpdated();
	}
	
}
