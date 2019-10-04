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
package nl.nikhef.safaripark.devmgr;

import java.awt.Window;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;

import nl.nikhef.safaripark.SaFariPark;
import nl.nikhef.safaripark.extra.SimProviderWindow;
import nl.nikhef.sfp.SFPDevice;
import nl.nikhef.sfp.SFPDeviceListener;
import nl.nikhef.sfp.SFPManager;
import nl.nikhef.sfp.SFPProvider;
import nl.nikhef.sfp.SFPProviderListener;
import nl.nikhef.sfp.SimSFPProvider;

public class DeviceManagerModel implements SFPProviderListener, SFPDeviceListener {
	
	private static final Logger LOG = Logger.getLogger(DeviceManagerModel.class.getSimpleName());

	private class RootTreeNode implements TreeNode {

		@Override
		public TreeNode getChildAt(int childIndex) {
			return _devices.get(childIndex);
		}

		@Override
		public int getChildCount() {
			return _devices.size();
		}

		@Override
		public TreeNode getParent() {
			return null;
		}

		@Override
		public int getIndex(TreeNode node) {
			return _devices.indexOf(node);
		}

		@Override
		public boolean getAllowsChildren() {
			return true;
		}

		@Override
		public boolean isLeaf() {
			return false;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public Enumeration children() {
			return Collections.enumeration(_devices);
		}
		
	}

	public class DeviceNode implements TreeNode 
	{

		private SFPDevice _dev;
		private DeviceBayNode[] _bayNode;
		
		public DeviceNode(SFPDevice dev){
			_dev = dev;
			_bayNode = new DeviceBayNode[_dev.getBayCount()];
			for (int i = 0; i < _bayNode.length; i++)
			{
				_bayNode[i] = new DeviceBayNode(this, i);
			}
		}

		@Override
		public TreeNode getChildAt(int childIndex) {
			return _bayNode[childIndex];
		}

		@Override
		public int getChildCount() {
			return _bayNode.length;
		}

		@Override
		public TreeNode getParent() {
			return _root;
		}

		@Override
		public int getIndex(TreeNode node) {
			for (int i = 0; i < _bayNode.length; i++) {
				if (_bayNode[i].equals(node)) return i;
			}
			return -1;
		}

		@Override
		public boolean getAllowsChildren() {
			return true;
		}

		@Override
		public boolean isLeaf() {
			return false;
		}

		@SuppressWarnings("rawtypes")
		@Override
		public Enumeration children() {
			return Collections.enumeration(Arrays.asList(_bayNode));
		}

		public SFPDevice getDevice() {
			return _dev;
		}

		
		@Override
		public String toString() {
			return _dev.getSerial();
		}

		public DeviceBayNode getBayNode(int bay) {
			return _bayNode[bay];
		}
	}
	
	public static class DeviceBayNode implements TreeNode
	{
		public int pos;
		private DeviceNode _parent;
		
		public DeviceBayNode(DeviceNode parent, int pos) {
			super();
			this.pos = pos;
			this._parent = parent;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + pos;
			return result;
		}
		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (!(obj instanceof DeviceBayNode))
				return false;
			DeviceBayNode other = (DeviceBayNode) obj;
			if (pos != other.pos)
				return false;
			return true;
		}
		
		
		@Override
		public String toString() {
			SFPDevice dev = _parent.getDevice();
			if (dev.isModulePresent(pos)) {
				
				String serial = dev.getModuleName(pos);
				
				// small window between isPresent and reading serial may allow for null value to be returned
				if (serial == null) serial = "<none>";
				
				return String.format("%d: %s", pos, serial);
			} else {
				return String.format("%d: <none>", pos);
			}
		}
		@Override
		public TreeNode getChildAt(int childIndex) {
			return null;
		}
		@Override
		public int getChildCount() {
			return 0;
		}
		@Override
		public TreeNode getParent() {
			return _parent;
		}
		@Override
		public int getIndex(TreeNode node) {
			return -1;
		}
		@Override
		public boolean getAllowsChildren() {
			return false;
		}
		@Override
		public boolean isLeaf() {
			return true;
		}
		@SuppressWarnings("rawtypes")
		@Override
		public Enumeration children() {
			return Collections.emptyEnumeration();
		}
		
	}
	
	
	
	private TreeNode _root = new RootTreeNode();
	private DefaultTreeModel _dtm = new DefaultTreeModel(_root, true);
	private SFPManager _mgr;
	
	private List<DeviceNode> _devices = new ArrayList<DeviceNode>(); 
	
	public DeviceManagerModel(SFPManager sfpManager) {
		_mgr = sfpManager;
		_mgr.addSFPProviderListener(this);
		populateDevices();
	}
	
	public TreeModel getTreeModel() {
		return _dtm;
	}
	
	
	private void addDevice(SFPDevice dev)
	{
		dev.addDeviceListener(this);
		_devices.add(new DeviceNode(dev));
	}
	
	private void removeDevice(SFPDevice dev)
	{
		dev.removeDeviceListener(this);
		_devices.remove(nodeForSfp(dev));
	}

	
	private SaFariPark getSaFariPark() {
		List<Window> visibleWindows = new ArrayList<Window>();
		for(Window w: Window.getWindows()){
		    if(w instanceof SaFariPark){
		    	return SaFariPark.class.cast(w);
		    }
		}
		return null;
	}
	
	
	public void populateDevices() 
	{
		for (SFPProvider prov : _mgr.getProviders()) {
			
			if (prov instanceof SimSFPProvider) {
				new SimProviderWindow(SimSFPProvider.class.cast(prov).getSimulatedDevice(), getSaFariPark());
			}

			for (SFPDevice dev: prov.getDevices())
			{
				addDevice(dev);
			}
		}
	}
	

	private DeviceNode nodeForSfp(SFPDevice dev)
	{
		for (DeviceNode devNode : _devices)
		{
			if (devNode.getDevice().equals(dev)) return devNode;
		}
		return null;
	}

	
	
	@Override
	public void sfpDeviceAdded(SFPProvider provider, SFPDevice dev) {
		
		if (_devices.contains(dev)) return;
		addDevice(dev);
		LOG.info(String.format("SFP Read-Out device: %s/%s bays=%d", provider, dev.getSerial(), dev.getBayCount()));

		_dtm.nodeStructureChanged(_root);
	}

	
	@Override
	public void sfpDeviceRemoved(SFPProvider provider, SFPDevice dev) 
	{
		removeDevice(dev);
		_dtm.nodeStructureChanged(_root);
	}

	@Override
	public void sfpModuleStateChanged(SFPDevice dev, int bay) {

		for (int i = 0; i < _devices.size(); i++)
		{
			if (_devices.get(i).getDevice() == dev)
			{
				_dtm.nodeChanged(
						_devices.get(i).getBayNode(bay));
			}
		}
	}

}
