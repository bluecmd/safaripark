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

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;

import nl.nikhef.sfp.ddmi.DDMIValue;

@SuppressWarnings("serial")
public class TextEditor extends ValueEditor implements DocumentListener {

	private static class JTextFieldLimit extends PlainDocument {

		private int _limit;

		private JTextFieldLimit(int limit) {
			super();
			_limit = limit;
		}


		public void insertString(int offset, String str, AttributeSet attr) throws BadLocationException {
			if (str == null)
				return;

			if ((getLength() + str.length()) <= _limit) {
				super.insertString(offset, str, attr);
			}
		}
	}
	
	private JTextField _tf;
	private boolean _ignoreEvents = false;
	
	public TextEditor(EditorContext ctx, DDMIValue value, JLabel label) {
		super(ctx, value, label);
		setLayout(new BorderLayout());
		_tf = new JTextField();
		_tf.setDocument(new JTextFieldLimit(value.getLength()));
		add(_tf, BorderLayout.CENTER);
		_tf.getDocument().addDocumentListener(this);
		clearValue();
	}

	
	@Override
	protected void clearValue() {
		_tf.setEnabled(false);
		_tf.setEditable(false);
		_ignoreEvents = true;
		_tf.setText("");		
		_ignoreEvents = false;
	}
	
	@Override
	protected void updateValue() 
	{
		_tf.setEnabled(true);
		_tf.setEditable(value.isWritable());
		_ignoreEvents = true;
		_tf.setText(getAsString());		
		_ignoreEvents = false;
	}

	private void valueUpdated() {
		if (_ignoreEvents) return;
		setAsString(_tf.getText());
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
