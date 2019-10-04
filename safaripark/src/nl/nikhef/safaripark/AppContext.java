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

import java.util.HashSet;
import java.util.Set;

import nl.nikhef.safaripark.extra.BaseMessage;
import nl.nikhef.safaripark.extra.Message;
import nl.nikhef.safaripark.extra.MessagePane;
import nl.nikhef.safaripark.extra.Module;
import nl.nikhef.safaripark.extra.StatefulTask;
import nl.nikhef.sfp.SFPManager;
import nl.nikhef.sfp.ddmi.DDMILoader;

public class AppContext {

	public final SFPManager sfpMgr;
	public final DDMILoader ddmiLdr;
	public final OverlayManager ovlMgr;
	public final ContextCache ctxCache;
	private SaFariPark _sfp;
	
	private final Set<Module> _locks = new HashSet();
	private MessagePane _msgPane;
	
	public AppContext(SFPManager sfpMgr, DDMILoader ddmiLdr, OverlayManager ovlMgr) {
		this.sfpMgr = sfpMgr;
		this.ddmiLdr = ddmiLdr;
		this.ovlMgr = ovlMgr;
		this.ctxCache = new ContextCache(sfpMgr, ddmiLdr);
	}
	
	
	public void setSaFariPark(SaFariPark sfp) {
		_sfp = sfp;
	}
	

	public SaFariPark getSaFariPark() {
		return _sfp;
	}
	
	public boolean isLocked(Module m)
	{
		return _locks.contains(m);
	}
	
	public boolean lock(Module m)
	{
		if (_locks.contains(m)) return false;
		_locks.add(m);
		return true;
	}
	
	public void unlock(Module m) 
	{
		if (!_locks.contains(m)) {
			throw new RuntimeException("Module unlocked w/o being locked");
		}
		_locks.remove(m);
	}

	
	public void setMessagePane(MessagePane msgPane)
	{
		_msgPane = msgPane;
	}
	

	
	public MessagePane getMessagePane() {
		return _msgPane;
	}

	private Message _lockedMessage = new BaseMessage("One or more modules is currently in use by another operation");

	public void showLockedMessage() {
		getMessagePane().addMessage(_lockedMessage);
	}



	

}
