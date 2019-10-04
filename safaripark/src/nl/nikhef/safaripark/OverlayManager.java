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
package nl.nikhef.safaripark;

import java.awt.Component;
import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import nl.nikhef.safaripark.extra.JCheckBoxList;
import nl.nikhef.safaripark.res.Resources;

public class OverlayManager {

	private Map<String, Boolean> _overlays = new HashMap<String, Boolean>();
	private File _overlayDir;
	private Preferences _prefs = Config.PREFS.node("overlays");
	
	public void scanDirectory(File dir) {
		_overlayDir = dir;
		
		String[] files = _overlayDir.list(new FilenameFilter() {
			
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".xml");
			}
		});
		
		for (String file : files)
		{
			_overlays.put(file, _prefs.getBoolean(file, false));
		}
		
	}
	
	public URL[] getOverlays()
	{
		List<URL> lst = new ArrayList<URL>();
		
		for (Map.Entry<String, Boolean> entries : _overlays.entrySet())
		{
			if (!entries.getValue()) continue;
			
			try {
				lst.add(new File(_overlayDir, entries.getKey()).toURI().toURL());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		
		return lst.toArray(new URL[lst.size()]);
	}
	
	public void showDialog(Component parent)
	{
		DefaultListModel<JCheckBox> cblm = new DefaultListModel<JCheckBox>();
		
		for (Map.Entry<String, Boolean> entries : _overlays.entrySet())
		{
			cblm.addElement(new JCheckBox(entries.getKey(), entries.getValue()));	
		}
		JCheckBoxList cbl = new JCheckBoxList(cblm);
		JScrollPane jsp = new JScrollPane(cbl);
		
		Component[] comp = new Component[] {
			jsp,
			new JLabel("Changes to overlays require a restart to be applied", Resources.getIcon("emblem-notice"),
					SwingConstants.CENTER)
		};
		
		if (JOptionPane.showOptionDialog(parent, comp, "Select overlays to load",
				JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null) != JOptionPane.OK_OPTION) {
			return;			
		}
		
		for (int i = 0; i < cblm.getSize(); i++)
		{
			JCheckBox cb = cblm.getElementAt(i);
			_overlays.put(cb.getText(), cb.isSelected());
			_prefs.putBoolean(cb.getText(), cb.isSelected());
		}
		
	}


	
}
