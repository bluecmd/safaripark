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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Action;
import javax.swing.SwingUtilities;

import nl.nikhef.safaripark.AppContext;
import nl.nikhef.safaripark.extra.Module;
import nl.nikhef.sfp.ddmi.DDMIContext;
import nl.nikhef.sfp.ddmi.DataSource;


/**
 * Links the editor to the I2CLink. 
 * 
 * @author Vincent van Beveren (v.van.beveren [at] nikhef.nl)
 */
public class EditorContext 
{

	
	private List<ValueEditor>	_editor	= new ArrayList<ValueEditor>();
	private Set<ValueEditor>	_dirty	= new HashSet<ValueEditor>();
	private DDMIContext			_ctx	= null;
	private boolean				_batchProc = false;
	private Action 				_apply;
	private Action 				_revert;
	private Action 				_reload;
	private AppContext			_appCtx;
	private boolean				_hasLock = false;
	private Module				_selected;
	
	public EditorContext(AppContext appCtx, Action apply, Action revert, Action reload) {
		_apply = apply;
		_revert = revert;
		_reload = reload;
		_appCtx = appCtx;
	}

	void addEditor(ValueEditor editor)
	{
		_editor.add(editor);
	}
	

	/**
	 * Set the link, or NULL if there is no link.
	 * 
	 * @param ctx		The Link.
	 */
	public void setContext(DDMIContext ctx, Module mod)
	{
		_ctx = ctx;
		
		freeLock();
		
		_selected = mod;		
		
		
		_dirty.clear();
		
		for (ValueEditor ve : _editor)
		{
			ve.updateEditor();
		}
	}

	
	private void freeLock() {
		if (!_hasLock) return;
			
		_appCtx.unlock(_selected);
		_hasLock = false;
	}

	/**
	 * Returns whether or not some components are dirty.
	 * 
	 * @return
	 */
	public boolean someAreDirty() 
	{
		return _dirty.size() != 0;
	}

	
	void updateButtons()
	{
		if (someAreDirty()) {
			_apply.setEnabled(true);
			_revert.setEnabled(true);
			_reload.setEnabled(false);
		} else {
			_apply.setEnabled(false);
			_revert.setEnabled(false);
			if (_ctx != null) {
				_reload.setEnabled(true);
			} else {
				_reload.setEnabled(false);
			}
		}
	}
	
	/**
	 *  
	 */
	public void commit() 
	{
		_batchProc = true;
		for (ValueEditor ve : _dirty)
		{
			ve.commit();
		}
		_dirty.clear();
		_batchProc = false;
		freeLock();
		updateButtons();
	}

	public void revert() 
	{
		_batchProc = true;
		for (ValueEditor ve : _dirty)
		{
			ve.revert();
		}
		_dirty.clear();
		_batchProc = false;
		freeLock();
		updateButtons();
	}

	public void reload() {
		
		if (_ctx == null) return;
		for (DataSource ds : _ctx.getDDMI().getSources()) {
			ds.clearCache(_ctx);
		}
		
		for (ValueEditor ve : _editor)
		{
			ve.updateEditor();
		}
	}
	
	
	public DDMIContext getContext()
	{
		return _ctx;
	}


	
	
	public void changeDirtyState(ValueEditor valueEditor) {
		if (_batchProc) return;
		
		if (valueEditor.dirty) 
		{
			if (!_hasLock) {
				if (_appCtx.isLocked(_selected)) {
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							revert();
						}	
					});
					
					_appCtx.showLockedMessage();
					return;
				}
				
				_appCtx.lock(_selected);
				_hasLock = true;
			}
			
			if (!_dirty.contains(valueEditor)) _dirty.add(valueEditor);
		}
		else
		{
			if (_dirty.contains(valueEditor)) _dirty.remove(valueEditor);
		}
		updateButtons();
	}

	
}
