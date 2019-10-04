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

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JToggleButton;

import nl.nikhef.sfp.SimSFPDevice;

@SuppressWarnings("serial")
public class SimProviderWindow extends JDialog implements ActionListener {

	
	
	private SimSFPDevice _dev;

	private List<JToggleButton> _toggles = new ArrayList<JToggleButton>();
	
	
	public SimProviderWindow(SimSFPDevice dev, JFrame parent) {
		super(parent);		
		setTitle("Sim SFP Control");
		setModal(false);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		_dev = dev;
		
		setLayout(new GridLayout(_dev.getBayCount(), 1));
		
		for (int i = 0; i < _dev.getBayCount(); i++) 
		{
			JToggleButton toggle = new JToggleButton(String.format("Insert into %d", i));
			toggle.addActionListener(this);
			_toggles.add(toggle);
			add(toggle);						
		}
		pack();
		setVisible(true);
	}


	
	@Override
	public void actionPerformed(ActionEvent e) 
	{
		JToggleButton tbut = (JToggleButton) e.getSource();
		int bay = _toggles.indexOf(e.getSource());
		if (_dev.isModulePresent(bay)) 
		{
			_dev.removeModule(bay);
			tbut.setText(String.format("Insert into %d", bay));
			tbut.setSelected(false);
		} else {
			_dev.placeModule(bay);
			tbut.setText(String.format("Remove from %d", bay));
			tbut.setSelected(true);
		}
	}
	
}
