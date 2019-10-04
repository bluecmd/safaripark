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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

import nl.nikhef.safaripark.AppContext;
import nl.nikhef.safaripark.ContextCache;
import nl.nikhef.safaripark.extra.Module;
import nl.nikhef.sfp.SFPDevice;
import nl.nikhef.sfp.SFPDeviceListener;
import nl.nikhef.sfp.SFPManager;
import nl.nikhef.sfp.SFPProvider;
import nl.nikhef.sfp.SFPProviderListener;
import nl.nikhef.sfp.ddmi.DDMI;
import nl.nikhef.sfp.ddmi.DDMIContext;
import nl.nikhef.sfp.ddmi.DDMIFilter;
import nl.nikhef.sfp.ddmi.DDMIUtils;
import nl.nikhef.sfp.ddmi.DDMIValue;

@SuppressWarnings("serial")
public class MonitorModel extends AbstractTableModel implements SFPDeviceListener, SFPProviderListener {

	private final DDMI _ddmi;
	private List<DDMIValue> _columns;
	
	private SortedSet<SFPDevice> _devices = new TreeSet<SFPDevice>();
	
	private ContextCache _ctxCache;
	
	private List<MonitoredModule> _rows = new ArrayList<MonitoredModule>();
	
	private class MonitoredModule extends Module
	{
		DDMIContext ctx;
		private String[] data;
		
		
		public MonitoredModule(SFPDevice dev, int bay) throws IOException {
			super(dev, bay);
			// this.dev.getLink(bay);
			ctx = _ctxCache.getContextFor(dev, bay);
		}
	}
	
	
	public MonitorModel(AppContext appCtx) {
		_ctxCache = appCtx.ctxCache;
		appCtx.sfpMgr.addSFPProviderListener(this);
		_ddmi = appCtx.ddmiLdr.getDDMI();
		_columns = _ddmi.findElements(new DDMIFilter<DDMIValue>() {

			@Override
			public boolean matches(DDMIValue element) {
				return element.isMonitor();
			}

			@Override
			public Class<DDMIValue> matchClass() {
				return DDMIValue.class;
			}
			
		});
		
	}
	
	


	public List<DDMIValue> getColumns() {
		return _columns;
	}
	
	public void setColumns(List<DDMIValue> columns) {
		_columns = columns;
		fireTableStructureChanged();
	}
	
	@Override
	public int getColumnCount() {
		return _columns.size() + 3;
	}
	
	@Override
	public String getColumnName(int column) {
		
		switch (column)
		{
		case 0: return "Module";
		case 1: return "TX";
		case 2: return "RX";
		default:
			return _columns.get(column - 3).getLabel();
		}
	}

	@Override
	public int getRowCount() {
		return _rows.size();
	}
	
	
	public void updateData()
	{
		for (int i = 0; i < _rows.size(); i++)
		{
			MonitoredModule dab = _rows.get(i);
			
			if (dab.data == null || dab.data.length != _columns.size()) 
			{
				dab.data = new String[_columns.size()];
			}
			
			for (int j = 0; j < _columns.size(); j++)
			{
				DDMIValue val = _columns.get(j);
				if (val.isValid(dab.ctx)) {
					try {
						dab.data[j] = DDMIUtils.getValueAsSting(val, dab.ctx);
					} catch (Exception e) {
						dab.data[j] = "?";
					}
				} else {
					dab.data[j] = "-";
				}
			}
		}
		fireTableDataChangedPreserveSelect();
	}
	

	@Override
	public Object getValueAt(int row, int col) {
		
		MonitoredModule dab = _rows.get(row);
		
		switch (col) {
		case 0:
			return dab.dev.getModuleName(dab.bay);
		case 1:
			return dab.isTxFault() ? "FAULT" : "OK";
		case 2:
			return dab.isRxLoss() ? "LOSS" : "OK";
		default:
			if (dab.data != null && (col - 3) < dab.data.length) 
			{
				return dab.data[col - 3];
			}
			return "?";
		}
	}


     public void fireTableDataChangedPreserveSelect() {
         fireTableChanged(new TableModelEvent(this, //tableModel
                                              0, //firstRow
                                              getRowCount() - 1, //lastRow 
                                              TableModelEvent.ALL_COLUMNS, //column 
                                              TableModelEvent.UPDATE)); //changeType
     }
	
	@Override
	public void sfpModuleStateChanged(SFPDevice dev, int bay) 
	{
		if (!_devices.contains(dev)) {
			_devices.add(dev);
		}
		if (updateRowCount()) {
			fireTableDataChanged();
		}
	}


	private boolean updateRowCount() {
		int rc = 0;
		
		List<MonitoredModule> dAb = new ArrayList<MonitoredModule>();
		
		for (SFPDevice dev : _devices)
		{
			for (int i = 0; i < dev.getBayCount(); ++i) 
			{
				if (dev.isModulePresent(i)) {
					
					if (!dev.hasDiagnostics(i)) continue;
					
					try {
						dAb.add(new MonitoredModule(dev, i));
						rc++;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		
		_rows = dAb;
		return true;
	}


	@Override
	public void sfpDeviceAdded(SFPProvider provider, SFPDevice dev) {
		_devices.add(dev);
		dev.addDeviceListener(this);
	}


	@Override
	public void sfpDeviceRemoved(SFPProvider provider, SFPDevice dev) {
		_devices.remove(dev);
		dev.removeDeviceListener(this);
	}


	public void writeHeader(PrintWriter logFile) {
		
		logFile.print("Module Serial");
		
		for (DDMIValue val : _columns) {
			logFile.print(";" + val.getName());
		}
		logFile.println();
	}

	public void writeData(PrintWriter logFile) 
	{
		for (int i = 0; i < _rows.size(); i++)
		{
			MonitoredModule dab = _rows.get(i);
			
			logFile.print(dab.dev.getModuleName(dab.bay));
			
			for (int j = 0; j < _columns.size(); j++)
			{
				logFile.print(";");
				logFile.print(dab.data[j]);
			}
			logFile.println();
		}
	}
	
	
}
