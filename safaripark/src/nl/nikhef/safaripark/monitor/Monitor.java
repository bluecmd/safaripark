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
package nl.nikhef.safaripark.monitor;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.swing.Action;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.SpinnerListModel;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumnModel;

import nl.nikhef.safaripark.AppContext;
import nl.nikhef.safaripark.ContextCache;
import nl.nikhef.safaripark.extra.ExtendedAbstractAction;
import nl.nikhef.safaripark.res.Resources;
import nl.nikhef.safaripark.vsp.ValueSelectionPane;
import nl.nikhef.sfp.SFPManager;
import nl.nikhef.sfp.ddmi.DDMI;
import nl.nikhef.sfp.ddmi.DDMIElement;
import nl.nikhef.sfp.ddmi.DDMILoader;
import nl.nikhef.sfp.ddmi.DDMIValue;
import nl.nikhef.tools.Filter;

@SuppressWarnings("serial")
public class Monitor extends JPanel implements ActionListener, ChangeListener {

	
	
	private JTable _table;
	private JToolBar _tbar;
	private Timer _timer;
	private ValueSelectionPane _vsp;
	private JFileChooser _fcStore;
	private DateFormat _fileDateFormat = new SimpleDateFormat("yyMMdd_HHmmss");
	private PrintWriter _logFile;
	
	
	private static class Rate {
		
		private final String name;
		private final int interval;
		
		public Rate(String name, int interval) {
			this.interval = interval;
			this.name = name;
		}
		
		@Override
		public String toString() {
			return this.name;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + interval;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Rate other = (Rate) obj;
			if (interval != other.interval)
				return false;
			return true;
		}
		
	}
	
	private Rate[] updateRates = new Rate[] {
			new Rate("1 second", 1), new Rate("2 seconds", 2), new Rate("5 seconds", 5), 
			new Rate("10 seconds", 10), new Rate("30 seconds", 30), new Rate("1 minute", 60), 
			new Rate("5 minutes", 300), new Rate("10 minutes", 600) };

	private JSpinner _interval;
	
	
	private Action _selectColumns = new ExtendedAbstractAction("Columns", Resources.getIcon("table-gear"), "Select which columns to show and log") {
		
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			_vsp.setSelection(_mm.getColumns());
			if (_vsp.showModel(Monitor.this)) {
				_mm.setColumns(_vsp.getSelection());				
			}
		}
	};

	private Action _recData = new ExtendedAbstractAction("Record", Resources.getIcon("media-record-2"), "Record data to file") {
		
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			startRecord();
		}
	};

	private Action _stopData = new ExtendedAbstractAction("Stop", Resources.getIcon("media-playback-stop-2"), "Stop recording data to file") {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			stopRecord();
		}
	};

	
	
	private MonitorModel _mm;
	
	public Monitor(AppContext appCtx) {
		setLayout(new BorderLayout());
		
		_mm = new MonitorModel(appCtx);
		_table = new JTable(_mm);
		// _table.setRowHeight(UIManager.getInt("Tree.rowHeight"));
		
		
		// align all numeric columns right
		DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		rightRenderer.setHorizontalAlignment( JLabel.RIGHT );
		
		TableColumnModel tcm = _table.getColumnModel();
		for (int i = 1; i < tcm.getColumnCount(); ++i)
			tcm.getColumn(i).setCellRenderer(rightRenderer);
		
		JScrollPane sp = new JScrollPane(_table);
		_tbar = new JToolBar("Monitor");
		_tbar.add(_selectColumns);
		_tbar.add(_recData);
		_tbar.add(_stopData);
		_stopData.setEnabled(false);
		_tbar.addSeparator();
		_tbar.add(new JLabel(" Update rate "));
		_interval = new JSpinner(new SpinnerListModel(updateRates));
		_interval.setValue(updateRates[2]);
		((JSpinner.DefaultEditor) _interval.getEditor()).getTextField().setEditable(false);
		Dimension d = _interval.getPreferredSize();
		d.width = 100;
		_interval.setPreferredSize(d);
		_interval.setMaximumSize(d);
		_tbar.add(_interval);
		_interval.addChangeListener(this);
		
		_tbar.setFloatable(false);
		_vsp = new ValueSelectionPane(appCtx.ddmiLdr);
		_vsp.init(new Filter<DDMIElement>() {

			@Override
			public boolean match(DDMIElement other) {
				
				if (!(other instanceof DDMIValue)) {
					return false;
				}
				DDMIValue v = DDMIValue.class.cast(other);
				
				return v.isMonitor();
			}

			@Override
			public Class<DDMIElement> getFilterClass() {
				return DDMIElement.class;
			}
			
		}, "monitor_cols");
		
		add(_tbar, BorderLayout.NORTH);
		add(sp, BorderLayout.CENTER);
		_table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		
		_fcStore = new JFileChooser();
		FileNameExtensionFilter filter = new FileNameExtensionFilter("Column Separated Values", "csv");
		_fcStore.setFileFilter(filter);
		_fcStore.setFileSelectionMode(JFileChooser.FILES_ONLY);
		_fcStore.setDialogTitle("Specify name of recording file");
				
		_timer = new Timer(5000, this);
		_timer.setRepeats(true);
		_timer.start();
		
		
	}
	
	private void startRecord()
	{
		String filename = "sfpdata_" + _fileDateFormat.format(new Date()) + ".csv";
		
		_fcStore.setSelectedFile(new File(filename));
		if (_fcStore.showSaveDialog(Monitor.this) != JFileChooser.APPROVE_OPTION) return;
		
		try {
			_logFile = new PrintWriter(_fcStore.getSelectedFile());
			_mm.writeHeader(_logFile);
		} catch (FileNotFoundException e) {
			// TODO  logging and error stuff
			e.printStackTrace();
			
			if (_logFile != null) {
				try {
					_logFile.close();
				} catch (Exception e2) {};
				_logFile = null;
			}
			
			return;
		}
		
		
		_recData.setEnabled(false);
		_stopData.setEnabled(true);
		_selectColumns.setEnabled(false);
		_interval.setEnabled(false);
	}
	
	private void stopRecord() {
		
		if (_logFile != null) {
			_logFile.close();
			_logFile = null;
		}
		
		_recData.setEnabled(true);
		_stopData.setEnabled(false);
		_selectColumns.setEnabled(true);
		_interval.setEnabled(true);
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		_mm.updateData();
		
		if (_logFile != null) {
			_mm.writeData(_logFile);
		}
	}


	@Override
	public void stateChanged(ChangeEvent e) {
		_timer.setInitialDelay(Rate.class.cast(_interval.getValue()).interval * 1000);
		_timer.setDelay(Rate.class.cast(_interval.getValue()).interval * 1000);
		_timer.restart();
	}
	
}
