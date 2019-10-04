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
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.filechooser.FileNameExtensionFilter;

import nl.nikhef.safaripark.devmgr.BaySelectionListener;
import nl.nikhef.safaripark.extra.BaseMessage;
import nl.nikhef.safaripark.extra.Message;
import nl.nikhef.safaripark.extra.Module;
import nl.nikhef.sfp.SFPDevice;
import nl.nikhef.sfp.ddmi.DDMI;
import nl.nikhef.sfp.ddmi.DDMIContext;
import nl.nikhef.sfp.ddmi.DDMILoader;
import nl.nikhef.sfp.ddmi.DataSource;
import nl.nikhef.tools.Utils;


public class ModuleManager implements BaySelectionListener 
{
	
	private Action _binExport;
	private Action _backup;
	private Action _restore;
	
	private JFileChooser _dirChooser;
	private JFileChooser _backupChooser;
	private JRadioButton _binary;
	private JRadioButton _plainhex;
	private JRadioButton _extendedhex;
	
	private DDMI _ddmi;
	private DDMIContext _ctx;
	private String _moduleName;
	private DateFormat _fileDateFormat = new SimpleDateFormat("yyMMdd_HHmmss");
	private AppContext _appCtx;
	private Module _selectedMod;

	public ModuleManager(Action binExport, Action backup, Action restore, AppContext appCtx)
	{
		_appCtx = appCtx;
		_binExport = binExport; 
		_binExport.setEnabled(false);
		
		_backup = backup; 
		_backup.setEnabled(false);
		_restore = restore; 
		_restore.setEnabled(false);

		
		
		_dirChooser = new JFileChooser();
		
		
		_ddmi = appCtx.ddmiLdr.getDDMI();
		
		ButtonGroup bg = new ButtonGroup();
		_binary = new JRadioButton("Binary (multiple files)");
		_plainhex = new JRadioButton("Plain Hex (multiple files)");
		_extendedhex = new JRadioButton("Extended Hex (single file)");
		bg.add(_plainhex);
		bg.add(_extendedhex);
		bg.add(_binary);
		_extendedhex.setSelected(true);

		JPanel pnl = new JPanel();
		pnl.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		pnl.setLayout(new BoxLayout(pnl, BoxLayout.PAGE_AXIS));		
		pnl.add(new JLabel("Export formats:"));
		pnl.add(_extendedhex);
		pnl.add(_plainhex);
		pnl.add(_binary);
		_dirChooser.setAccessory(pnl);
		
		_dirChooser.setDialogTitle("Select directoy to export module contents to");
		_dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		
		_backupChooser = new JFileChooser();
		_backupChooser.setFileFilter(new FileNameExtensionFilter("SaFariPark backup", "sfpb"));
	}


	@Override
	public void baySelected(SFPDevice dev, int bay) {
		try {
			if (dev.isModulePresent(bay)) 
			{
				_ctx = _appCtx.ctxCache.getContextFor(dev, bay);
				
				_selectedMod = new Module(dev, bay);
				_moduleName = dev.getModuleName(bay);
				_binExport.setEnabled(true);
				_backup.setEnabled(true);
				_restore.setEnabled(true);
				widthdrawChecksum();
				checkChecksums();
			} else {
				_ctx = null;
				_binExport.setEnabled(false);
				_backup.setEnabled(false);
				_restore.setEnabled(false);
				_selectedMod = null;
				widthdrawChecksum();
			}
		} catch (IOException e) {
			_ctx = null;
			_binExport.setEnabled(false);
			_backup.setEnabled(false);
			_restore.setEnabled(false);
			widthdrawChecksum();
		}

	}
	

	private class CheckSumMessage extends BaseMessage
	{

		public CheckSumMessage() {
			super("<html>Checksum for selected module does not check out, see Logging window for details.<br/><b>Note:</b> This could also occur if an incompatible overlay is loaded</html>", "Fix");
		}
		
		@Override
		public void runOperation() {
			if (_appCtx.isLocked(_selectedMod)) {
				_appCtx.showLockedMessage();
				return;
			}
			
			_ctx.updateChecksums();
		}
		
	}
	
	private Message _checksumMessage;
	
	public void checkChecksums()
	{
		if (!_ctx.verifyChecksums()) {
			_checksumMessage = new CheckSumMessage();
			_appCtx.getMessagePane().addMessage(_checksumMessage);
		}
	}
	
	private void widthdrawChecksum() {
		if (_checksumMessage != null) {
			_appCtx.getMessagePane().widthdrawMessage(_checksumMessage);
		}
	}

	
	public void exportBinary(Component parent)
	{
		if ((_dirChooser.showDialog(parent, "Select") != JFileChooser.APPROVE_OPTION)) 
		{
			return;
		}
		
		File f = _dirChooser.getSelectedFile();
		
		
		
		String filebasebase =String.format("sfp_%s_%s", _moduleName, _fileDateFormat.format(new Date())); 
		
		filebasebase = filebasebase.replace('/', '_');
		filebasebase = filebasebase.replace('\\', '_');
		
		List<String> filesWritten = new ArrayList<String>();
		
		List<DataSource> orderedSources = new ArrayList<DataSource>();
		
		orderedSources.addAll(_ddmi.getSources());
		
		Comparator<DataSource> comparator = new Comparator<DataSource>() {
		    @Override
		    public int compare(DataSource left, DataSource right) {
		        return left.getPath().compareTo(right.getPath());
		    }
		};
		
		Collections.sort(orderedSources, comparator);

		
		if (_extendedhex.isSelected()) {
			
			String filename = filebasebase + ".txt";

			try {
				PrintWriter pw = new PrintWriter(new FileWriter(new File(f, filename)));

				
				try {
					for (DataSource ds : orderedSources) 
					{
						if (!ds.isValid(_ctx)) continue;
						// System.out.println("Exporting datasource " + ds);
						
						try {
			
							byte[] data = ds.read(_ctx, ds.start, ds.size());
							
							pw.println("Section: " + ds.getPath());
							exportHex(f, pw, data, true, ds.start);	
							pw.println();
						} catch (IOException e) {
							e.printStackTrace();
							continue;
						}
						
					}
				} finally {
					filesWritten.add(filename);
					try {
						pw.close();
					} catch (Exception e) {};
				}
			} catch (IOException io) {
				// TODO log!
			}
			
		} else {
		
			for (DataSource ds : orderedSources) 
			{
				if (!ds.isValid(_ctx)) continue;
				// System.out.println("Exporting datasource " + ds);
		
				try {
	
					byte[] data = ds.read(_ctx, ds.start, ds.size());
					String filebase = filebasebase + "_" + ds.getPath();
					
					if (_plainhex.isSelected()) {
						exportHex(filesWritten, f, filebase, data, false, 0);	
					}
					if (_binary.isSelected()) {
						exportBinary(filesWritten, f, filebase, data);
					}
	
				} catch (IOException e) {
					e.printStackTrace();
					continue;
				}
				
			}
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("Following file(s) have been written:\n");
		for (String s : filesWritten) {
			sb.append(" * " + s + "\n");
		}
		sb.append("In directory: \n");
		sb.append("  " + f.getPath());
		
		JOptionPane.showMessageDialog(parent, sb.toString(), "Export success!", JOptionPane.INFORMATION_MESSAGE);	
	
	}

	
	
	private void exportHex(List<String> filesWritten, File f, String filebase, byte[] content, boolean extended, int offset) throws IOException 
	{
		PrintWriter pw = new PrintWriter(new FileWriter(new File(f, filebase + (extended ? ".txt" : ".hex"))));
		try {
			exportHex(f, pw, content, extended, offset);
			filesWritten.add(filebase +  (extended ? ".txt" : ".hex"));
		} finally {
			pw.close();
		}
	}


	private void exportHex(File f, PrintWriter pw, byte[] content, boolean extended, int offset) throws IOException 
	{
		Utils.dumpHex(pw, content, extended, offset);
	}
	
	
	private void exportBinary(List<String> filesWritten, File f, String filebase, byte[] content) throws IOException 
	{
		FileOutputStream fos = new FileOutputStream(new File(f, filebase + ".bin"));
		try {
			fos.write(content);
			filesWritten.add(filebase + ".bin");
		} finally {
			fos.close();
		}
	}
	
	@Override
	public boolean canSelectBay(SFPDevice dev, int bay) {
		return true;
	}


	public void backupBinary(Component parent) 
	{
		_backupChooser.setDialogTitle("Create backup");
		

		
		File file = null;
		String filebasebase =String.format("%s_%s.sfpb", _fileDateFormat.format(new Date()), _moduleName); 
		
		filebasebase = filebasebase.replace('/', '_');
		filebasebase = filebasebase.replace('\\', '_');

		
		while (file == null)
		{
			
			file = new File(filebasebase);

			_backupChooser.setSelectedFile(file);

			if ((_backupChooser.showDialog(parent, "Backup") != JFileChooser.APPROVE_OPTION)) 
			{
				return;
			}
			
			file = _backupChooser.getSelectedFile();
			
			if (file.exists()) {
				if (JOptionPane.showConfirmDialog(parent, "Do you want to overwrite?","File already exists!", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
					file = null;
				}
			}
		}
		
		try {
			DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
			try {
			
				dos.writeBytes("sfpb");	// signature
				dos.writeShort(0x100);	// version
	
				// for each source
				for (DataSource ds : _ddmi.getSources()) 
				{
					if (!ds.isValid(_ctx)) continue;
					byte[] data = ds.read(_ctx, ds.start, ds.size());
					
					// write path name
					dos.writeUTF(ds.getPath());
					// length
					dos.writeShort(data.length);
					// and data
					dos.write(data, 0, data.length);
					
				}
				dos.writeUTF("");
				JOptionPane.showMessageDialog(parent, "File written:\n" + file, "Backup success!", JOptionPane.INFORMATION_MESSAGE);
			} finally {
				try {
					dos.close();
				} catch (Exception e) {};
				
			}
		} catch (IOException io) {
			throw new RuntimeException("Failed to backup", io);
		}
	}


	public void restoreBinary(Component parent) {
		_backupChooser.setDialogTitle("Restore backup");
		
		_backupChooser.setSelectedFile(null);
		
		File file = null;
		
		while (file == null)
		{
			
			if ((_backupChooser.showDialog(parent, "Restore") != JFileChooser.APPROVE_OPTION)) 
			{
				return;
			}
			
			file = _backupChooser.getSelectedFile();
			
			if (!file.exists()) {
				JOptionPane.showMessageDialog(parent, "File " + file + " doest not exist", "Error", JOptionPane.ERROR_MESSAGE);
				file = null;
			}
		}
		
		Map<String, byte[]> backup = new HashMap<String, byte[]>();
		
		
		try {
			DataInputStream dis = new DataInputStream(new FileInputStream(file));
			
			try {
				byte[] signature = new byte[4];
				dis.readFully(signature);
				
				if (! new String(signature).equals("sfpb")) throw new RuntimeException("Invalid backup");
				if (dis.readShort() != 0x100) throw new RuntimeException("Backup version not supported");
				
				String section = dis.readUTF();
				
				while (!section.equals("") ) 
				{
					
					byte[] data = new byte[dis.readShort()];
					dis.readFully(data);
					
					backup.put(section, data);
					
					// for legacy format
					if (dis.available() == 0) break;
					
					section = dis.readUTF();		
				}
			} finally {
				try {
					dis.close();
				} catch (Exception e) {};
			}
		} catch (IOException io) {
			throw new RuntimeException("Failed to restore", io);
		}

		if (_appCtx.isLocked(_selectedMod)) {
			_appCtx.showLockedMessage();
			return;
		}
		
		StringBuilder sb = new StringBuilder();
		
		for (DataSource ds : _ddmi.getSources()) 
		{
			
			if (!ds.isValid(_ctx)) continue;
			
			if (!backup.containsKey(ds.getPath())) {
				// log warning
				sb.append("Section " + ds.getPath() + " not present in backup, kept as is\n");
				continue;
			}
			
			byte[] data = backup.get(ds.getPath());
			
			backup.remove(ds.getPath());
			
			if (data.length != ds.size()) {
				// log warning
				sb.append("Section " + ds.getPath() + " has different size from backup, skipped\n");
				continue;
			}
			try {			
				ds.write(_ctx, ds.start, data);
				Thread.sleep(100);
			} catch (Exception e) 
			{
				sb.append("Aborted due to error: " + e + "\n");
				break;
			}
		}
		
		if (backup.size() > 0) 
		{
			sb.append("Sections " + String.join(",", backup.keySet()) + " not restored");						
		}
		
		if (sb.length() > 0) {
			JOptionPane.showMessageDialog(parent, "Backup had warnings/errors:\n" + sb.toString() + 
					"\nYou need to reload the editor to see the new values", "Restore (partially) failed", JOptionPane.WARNING_MESSAGE);
		} else {
			JOptionPane.showMessageDialog(parent, "Backup from " + file + " restored" +
					"\nYou need to reload the editor to see the new values", "Restore success!", JOptionPane.INFORMATION_MESSAGE);
		}
		
	}

}
