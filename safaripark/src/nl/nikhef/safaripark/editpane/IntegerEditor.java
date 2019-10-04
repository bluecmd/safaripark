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
import java.text.DecimalFormat;
import java.text.NumberFormat;

import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.NumberFormatter;

import nl.nikhef.sfp.ddmi.DDMIValue;

@SuppressWarnings("serial")
public class IntegerEditor extends ValueEditor implements DocumentListener {

	private JFormattedTextField _tf;
	private boolean _ignoreEvents;
	
	public IntegerEditor(EditorContext ctx, DDMIValue value, JLabel label) {
		super(ctx, value, label);
		setLayout(new BorderLayout());
		DecimalFormat dec = new DecimalFormat();     
		dec.setGroupingUsed(false);
		_tf = new JFormattedTextField(new NumberFormatter(dec));
		add(_tf, BorderLayout.CENTER);
		_tf.getDocument().addDocumentListener(this);
		clearValue();
	}

	@Override
	protected void clearValue() 
	{
		_tf.setEnabled(false);
		_tf.setEditable(false);
		_ignoreEvents = true;
		_tf.setValue(0);
		_ignoreEvents = false;

	}

	@Override
	protected void updateValue() 
	{
		_tf.setEnabled(isValueValid());
		_tf.setEditable(value.isWritable());
		_ignoreEvents = true;
		_tf.setValue(getAsInt());
		_ignoreEvents = false;
	}

	
	private void valueUpdated() 
	{
		if (_ignoreEvents) return;
		int v = Number.class.cast(_tf.getValue()).intValue();
		setAsInt(v);
	}
	
	@Override
	public void insertUpdate(DocumentEvent e) 
	{
		valueUpdated();
	}

	@Override
	public void removeUpdate(DocumentEvent e) 
	{
		valueUpdated();
	}

	@Override
	public void changedUpdate(DocumentEvent e) 
	{
		valueUpdated();
	}

}
