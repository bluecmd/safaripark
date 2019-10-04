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
package nl.nikhef.safaripark.dolly;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.filechooser.FileNameExtensionFilter;

import net.miginfocom.swing.MigLayout;
import nl.nikhef.safaripark.AppContext;
import nl.nikhef.safaripark.SaFariPark;
import nl.nikhef.safaripark.Title;
import nl.nikhef.safaripark.extra.CheckableList;
import nl.nikhef.safaripark.extra.GuiUtils;
import nl.nikhef.safaripark.extra.Module;
import nl.nikhef.safaripark.res.Resources;
import nl.nikhef.safaripark.vsp.ValueSelectionPane;
import nl.nikhef.sfp.ddmi.DDMIElement;
import nl.nikhef.sfp.ddmi.DDMILoader;
import nl.nikhef.sfp.ddmi.DDMIValue;
import nl.nikhef.tools.Filter;

public class DollyPanel extends JPanel implements ActionListener, ItemListener {

	private ValueSelectionPane _vsp;
	
	
	private JRadioButton 			_sourceFileOption = new JRadioButton("File:");
	private JTextField				_sourceFileName = new JTextField("<none>");
	private JButton      			_sourceFileSelect = new JButton("...");
	private JRadioButton			_sourceModuleOption = new JRadioButton("Device:");
	private JComboBox<Module>		_sourceModule;
	private File					_sourceFile;
	
	private JCheckBox    			_targetFileOption = new JCheckBox("File: ");
	private JTextField	 			_targetFileName = new JTextField("<none>");
	private JButton      			_targetFileSelect = new JButton("...");
	private CheckableList<Module>	_targetModuleSelects;
	private File					_targetFile;
	
	private JButton      			_clone = new JButton("Clone");
	private JLabel					_errorMsg = new JLabel();
	
	private DollyModel   			_model;
	
	private JFileChooser			_fileChooser;
	private AppContext				_app;
	
	
	public DollyPanel(DollyModel model, AppContext app) {
		
		_model = model;
		_app = app;
		setLayout(new BorderLayout());
		add(new Title("Clone Tool", Resources.getIcon("emblem-documents")), BorderLayout.NORTH);
		
		_vsp = new ValueSelectionPane(_app.ddmiLdr);
		
		JPanel core = new JPanel();
		core.setLayout(new BorderLayout());
		
		JPanel source = new JPanel();
		source.setLayout(new MigLayout("","[][grow][]",""));
		source.add(new JLabel("Source"), "wrap");
		source.add(new JSeparator(),"growx, span 3, wrap");
		ButtonGroup bg = new ButtonGroup();
		bg.add(_sourceModuleOption);
		bg.add(_sourceFileOption);
		_sourceModuleOption.setSelected(true);
		source.add(_sourceModuleOption);
		_sourceModule = new JComboBox<Module>(_model.getInputComboboxModel());
		source.add(_sourceModule,"growx, span 2,wrap");
		source.add(_sourceFileOption);
		source.add(_sourceFileName, "growx");
		_sourceFileName.setEditable(false);
		source.add(_sourceFileSelect,"wrap");
		
		core.add(source, BorderLayout.NORTH);
		
		_vsp.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
		core.add(_vsp, BorderLayout.CENTER);
		
		
		JPanel target = new JPanel();
		target.setLayout(new MigLayout("", "[][grow][]", ""));
		target.add(new JLabel("Targets"), "wrap");
		target.add(new JSeparator(),"growx, span 3, wrap");
		target.add(new JLabel("Devices: "));
		_targetModuleSelects = new CheckableList<Module>(_model.getOutputListModel());
		
		JScrollPane jsp = new JScrollPane(_targetModuleSelects);
		
		target.add(jsp,"growx, span 2, wrap, height :100:");
		target.add(_targetFileOption);
		target.add(_targetFileName, "growx");
		_targetFileName.setEditable(false);
		target.add(_targetFileSelect,"wrap");
		
		core.add(target, BorderLayout.SOUTH);		
		
		
		_vsp.init(new Filter<DDMIElement>() {

			@Override
			public boolean match(DDMIElement other) {
				
				if (!(other instanceof DDMIValue)) {
					return false;
				}
				DDMIValue v = DDMIValue.class.cast(other);
				
				return v.isWritable();
			}

			@Override
			public Class<DDMIElement> getFilterClass() {
				return DDMIElement.class;
			}
			
		}, "dolly");
		
		_vsp.addItemListener(this);
		
		add(core, BorderLayout.CENTER);
		
		JPanel buttons = new JPanel();
		buttons.setLayout(new BorderLayout());
		
		
		_errorMsg.setIcon(Resources.getIcon("emblem-important-3"));
		_errorMsg.setForeground(Color.RED);
		_errorMsg.setVisible(false);
		buttons.add(_errorMsg, BorderLayout.CENTER);
		buttons.add(_clone, BorderLayout.EAST);
		add(buttons, BorderLayout.SOUTH);
		
		_fileChooser = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("SFP Module values", "smv");
		_fileChooser.setFileFilter(filter);
		
		
		_sourceModuleOption.addActionListener(this);
		_sourceFileOption.addActionListener(this);
		_targetFileOption.addActionListener(this);
		
		_targetModuleSelects.addItemListener(this);
		_sourceModule.addItemListener(this);
		
		_targetFileSelect.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				selectTargetFile();
			}
		});
		
		_sourceFileSelect.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				selectSourceFile();
				
			}
		});
		
		_clone.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				_app.getSaFariPark().executeTask(_model.getCloneTask());				
			}
		});
		
		updateAll();
	}
	
	protected void selectSourceFile() 
	{
		if (_fileChooser.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

		_sourceFile = _fileChooser.getSelectedFile();
		_sourceFileName.setText(_sourceFile.getPath());
		
		updateAll();		
	}

	protected void selectTargetFile() {
		if (_fileChooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
		
		_targetFile = _fileChooser.getSelectedFile();
		
		String targetFileName = _targetFile.getPath(); 
		if (!targetFileName.toLowerCase().endsWith(".smv")) {
			_targetFile = new File(targetFileName + ".smv");
		}
		
		_targetFileName.setText(_targetFile.getPath());
		
		updateAll();		
	}

	private void updateEnabled() {
		_sourceModule.setEnabled(_sourceModuleOption.isSelected());
		_sourceFileSelect.setEnabled(_sourceFileOption.isSelected());
		_targetFileSelect.setEnabled(_targetFileOption.isSelected());
	}

	private void updateModel() {
		if (_sourceModuleOption.isSelected()) 
		{
			// source module must be selected
			Module m = Module.class.cast(_sourceModule.getSelectedItem());
			_model.setSourceModule(m);
		} else {
			_model.setSourceFile(_sourceFile);
		}
		
		if (_targetFileOption.isSelected()) {
			_model.setTargetFile(_targetFile);
		}
		_model.setTargetModules(_targetModuleSelects.getSelectedItems());
		
		_model.setSelectedValues(_vsp.getSelection());

		if (_model.canRun()) {
			_clone.setEnabled(true);
			_errorMsg.setVisible(false);
		} else {
			_clone.setEnabled(false);
			_errorMsg.setText(_model.getErrorMessage());
			_errorMsg.setVisible(true);
		}
	}
	
	private void updateAll() {
		updateModel();
		updateEnabled();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) 
	{
		updateAll();		
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		updateAll();		
	}
	

}

