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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedList;
import java.util.Queue;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;

public class MessagePane extends JPanel implements ActionListener {

	private static final Color NOTE_YELLOW = new Color(255, 255, 220);
	private JLabel _message;
	private JButton _action;
	private JButton _close;
	
	 
	private LinkedList<Message> _messages = new LinkedList<Message>();
	
	public MessagePane() {
		Border outside = BorderFactory.createMatteBorder(1, 1, 1, 1, Color.BLACK);
		Border inside = BorderFactory.createEmptyBorder(2, 4, 2, 4); 
		setBorder(BorderFactory.createCompoundBorder(outside, inside));
		
		setBackground(NOTE_YELLOW);
		setForeground(Color.BLACK);
				
		setLayout(new BorderLayout());
		_message = new JLabel("One or more checksums are invalid, do you wish to fix this?");
		add(_message, BorderLayout.CENTER);
		
		JPanel btnPane = new JPanel();
		btnPane.setOpaque(false);
		_action = new JButton(" Fix! ");
		_action.setOpaque(false);
		_action.addActionListener(this);
		_close = new JButton("x");
		_close.setFont(_close.getFont().deriveFont(Font.BOLD));
		Border bevel = BorderFactory.createBevelBorder(BevelBorder.RAISED);
		
		
		_close.setBorder(BorderFactory.createCompoundBorder(bevel, BorderFactory.createEmptyBorder(1, 2, 2, 2)));
		_close.setContentAreaFilled(false);
		
        _close.setFocusPainted(false);
        _close.setOpaque(true);
		_close.setBackground(Color.RED);
		_close.setForeground(Color.WHITE);
		_close.addActionListener(this);
		
		btnPane.add(_action);
		btnPane.add(_close);
		
		add(btnPane, BorderLayout.EAST);
		setVisible(false);
	}
	
	public void addMessage(Message m)
	{
		if (_messages.contains(m)) return;
		_messages.addFirst(m);
		updateQueue();
		/*Runnable sound2 =
			      (Runnable)Toolkit.getDefaultToolkit().getDesktopProperty("win.sound.default");
		if(sound2 != null) sound2.run();*/
	}
	
	public void widthdrawMessage(Message m)
	{
		_messages.remove(m);
		updateQueue();
	}

	private void updateQueue() {
		if (_messages.size() > 0) {
			Message m = _messages.peek();
			_message.setText(m.getMessage());
			if (m.hasOperation()) {
				_action.setText(m.getOperationText());
				_action.setVisible(true);
			} else {
				_action.setVisible(false);
			}
			setVisible(true);
		} else {
			setVisible(false);
		}
	}

	@Override
	public void actionPerformed(ActionEvent e) 
	{
		Message m = _messages.poll();
		assert(m != null);
		if (e.getSource() == _action) {
			m.runOperation();
		} else if (e.getSource() == _close) {
			m.repelOperation();
		}
		
		updateQueue();
	}

}
