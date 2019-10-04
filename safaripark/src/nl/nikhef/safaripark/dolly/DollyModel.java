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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;

import nl.nikhef.safaripark.AppContext;
import nl.nikhef.safaripark.ContextCache;
import nl.nikhef.safaripark.extra.BaseMessage;
import nl.nikhef.safaripark.extra.Module;
import nl.nikhef.safaripark.extra.StatefulTask;
import nl.nikhef.sfp.SFPDevice;
import nl.nikhef.sfp.SFPDeviceListener;
import nl.nikhef.sfp.SFPProvider;
import nl.nikhef.sfp.SFPProviderListener;
import nl.nikhef.sfp.ddmi.DDMIValue;

public class DollyModel implements SFPDeviceListener, SFPProviderListener {

	private static final Logger LOG = Logger.getLogger(DollyModel.class.getSimpleName());
	
	private DefaultComboBoxModel<Module>	_dcb = new DefaultComboBoxModel<Module>();
	
	private DefaultListModel<Module>		_dlm = new DefaultListModel<Module>();
	private File							_sourceFile;
	private Module							_sourceModule;
	private File _targetFile;
	private Collection<Module> 				_targetModules;
	private String _errorMessage;
	private List<DDMIValue> _selectedValues;
	private AppContext _appCtx;
	
	public DollyModel(AppContext appCtx) 
	{
		_appCtx = appCtx;		
	}
	
	
	public ComboBoxModel<Module> getInputComboboxModel() {
		return _dcb;
	}
	
	public ListModel<Module> getOutputListModel() {
		return _dlm;
	}
	
	

	@Override
	public void sfpModuleStateChanged(SFPDevice dev, int bay) 
	{
		
		Module mod = new Module(dev, bay);
		
		if (dev.isModulePresent(bay)) 
		{
			_dcb.addElement(mod);
			_dlm.addElement(mod);
		} else {
			_dcb.removeElement(mod);
			_dlm.removeElement(mod);
		}
	}



	@Override
	public void sfpDeviceAdded(SFPProvider provider, SFPDevice dev) {
		dev.addDeviceListener(this);
	}


	@Override
	public void sfpDeviceRemoved(SFPProvider provider, SFPDevice dev) {
		dev.removeDeviceListener(this);
	}


	public void setSourceModule(Module m) {
		_sourceModule = m;
		_sourceFile = null;
	}


	public void setSourceFile(File sourceFile) {
		_sourceFile = sourceFile;
		_sourceModule = null;
	}


	public void setTargetFile(File targetFile) {
		_targetFile = targetFile;
	}


	private void checkErrors() 
	{
		_errorMessage = null;
		
		if (_sourceFile == null && _sourceModule == null) {
			_errorMessage = "No valid source";
			return;
		}
		if (_sourceFile != null && !_sourceFile.isFile()) {
			_errorMessage = "Source file does not exist";
			return;
		}
		
		if ((_targetModules == null || _targetModules.isEmpty()) && _targetFile == null) {
			_errorMessage = "No target specified";
			return;
		}
		
		if (_targetModules != null && _targetModules.contains(_sourceModule)) {
			_errorMessage = "Target can not be source";
			return;
		}
		
		if (_selectedValues == null || _selectedValues.size() == 0) 
		{
			_errorMessage = "No values selected";
			return;
		}
		
	}
	
	public boolean canRun() {
		
		checkErrors();
		return _errorMessage == null;
	}
	
	public String getErrorMessage() {
		return _errorMessage;
	}


	public void setTargetModules(Collection<Module> selectedItems) {
		_targetModules = selectedItems;
	}


	public void setSelectedValues(List<DDMIValue> selection) {
		_selectedValues = selection;		
	}
	
	
	private enum CloneState {
		PREPARE, CLONE, FINISH
	}

	private class CloneTask implements StatefulTask
	{

		private CloneState _state = CloneState.PREPARE;

		private ValueGetter _vg;
		private List<ValueSetter> _vs = new ArrayList<ValueSetter>();
		private int _counter = 0;
		
		public CloneTask()
		{
			
		}
		
		@Override
		public int getProgress() {
			
			switch (_state) {
			case PREPARE:
				return 0;
			case CLONE:
				return (_counter * 100) / _selectedValues.size();
			case FINISH:
				return 100;
			default:
				return -1;
			}
		}
		
		@Override
		public boolean execute() {
			
			switch (_state)
			{
				case PREPARE:
					
					
					
					if (_sourceFile != null) {
						try {
							_vg = new FileValueGetter(_sourceFile);
						} catch (IOException e) {
							LOG.severe(String.format("Clone failed, could not load file %s: %s", _sourceFile.getPath(), e));
							return true;
						}
					} else {
						
						if (_appCtx.isLocked(_sourceModule)) {
							_appCtx.showLockedMessage();
							return true;
						}
						
						_vg = new ModuleValueGetter(_appCtx.ctxCache, _sourceModule);			
					}
					
					for (Module m : _targetModules) {
						
						if (_appCtx.isLocked(m)) {
							_appCtx.showLockedMessage();
							return true;
						}
						
						_vs.add(new ModuleValueSetter(_appCtx.ctxCache, m));
					}
					
					// at this point all locks should be tested, so if this fails, it asserts false
					if (_sourceModule != null) if (!_appCtx.lock(_sourceModule)) assert(false); 
					for (Module m : _targetModules) if (!_appCtx.lock(m)) assert(false);
					
					if (_targetFile != null) {
						_vs.add(new FileValueSetter(_targetFile));
					}
					_state = CloneState.CLONE;
					break;
				case CLONE:
					
					
					DDMIValue v = _selectedValues.get(_counter);
					
					LOG.info("Copying value " + v.getName());
					byte[] value;
					try {
						value = _vg.getValue(v);
					} catch (IOException e) {
						LOG.severe("Clone failed : " + e);
						break;
					}
					if (value != null && value.length > 0)  {
						for (ValueSetter setter : _vs) {
							try {
								setter.setValue(v, value);
							} catch (IOException e) {
								LOG.warning(String.format("Failed to set value %s to %s: %s", v, setter, e));
							}
						}
					}
					_counter++;
					if (_counter >= _selectedValues.size()) {
						_state = CloneState.FINISH;	
					}
					break;
				case FINISH:
					for (ValueSetter setter : _vs) {
						try {
							setter.finished();
						} catch (IOException e) {
							LOG.warning(String.format("Failed to finish %s: %s", setter, e));
						}

					}
					if (_sourceModule != null) _appCtx.unlock(_sourceModule);
					for (Module m : _targetModules) _appCtx.unlock(m);
					
					LOG.info(String.format("Cloned %d value(s) to %d output(s)", _selectedValues.size(), _vs.size()));
					return true;
			}
			
			return false;
		}
		
	}
	
	public StatefulTask getCloneTask()
	{
		
		
		return new CloneTask();
	}
	
	public void doClone() {
		
		ValueGetter vg;
		
		if (_sourceFile != null) {
			try {
				vg = new FileValueGetter(_sourceFile);
			} catch (IOException e) {
				LOG.severe(String.format("Clone failed, could not load file %s: %s", _sourceFile.getPath(), e));
				return;
			}
		} else {
			vg = new ModuleValueGetter(_appCtx.ctxCache, _sourceModule);			
		}
		
		List<ValueSetter> vs = new ArrayList<ValueSetter>();
		
		
		for (Module m : _targetModules) {
			vs.add(new ModuleValueSetter(_appCtx.ctxCache, m));
		}
		if (_targetFile != null) {
			vs.add(new FileValueSetter(_targetFile));
		}
		
		
		for (DDMIValue v : _selectedValues)
		{
			
			LOG.info("Copying value " + v.getLabel());
			byte[] value;
			try {
				value = vg.getValue(v);
			} catch (IOException e) {
				LOG.severe("Clone failed : " + e);
				break;
			}
			if (value == null || value.length == 0) continue;
			for (ValueSetter setter : vs) {
				try {
					setter.setValue(v, value);
				} catch (IOException e) {
					LOG.warning(String.format("Failed to set value %s to %s: %s", v, setter, e));
				}
			}
		}

		for (ValueSetter setter : vs) {
			try {
				setter.finished();
			} catch (IOException e) {
				LOG.warning(String.format("Failed to finish %s: %s", setter, e));
			}

		}
		
		LOG.info(String.format("Cloned %d value(s) to %d output(s)", _selectedValues.size(), vs.size())); 		
	}





}
