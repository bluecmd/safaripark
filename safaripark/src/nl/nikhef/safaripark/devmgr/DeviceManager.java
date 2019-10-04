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

import java.awt.BorderLayout;
import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;

import nl.nikhef.safaripark.SaFariPark;
import nl.nikhef.safaripark.Title;
import nl.nikhef.safaripark.devmgr.DeviceManagerModel.DeviceBayNode;
import nl.nikhef.safaripark.devmgr.DeviceManagerModel.DeviceNode;
import nl.nikhef.safaripark.res.Resources;
import nl.nikhef.sfp.SFPDevice;
import nl.nikhef.sfp.SFPDeviceListener;
import nl.nikhef.sfp.SFPManager;
import nl.nikhef.tools.AWTDispatcher;
import nl.nikhef.tools.ListenerManager;

@SuppressWarnings("serial")
public class DeviceManager extends JPanel implements TreeSelectionListener, SFPDeviceListener, TreeModelListener {

	private DeviceManagerModel _devMgrMod;
	private JTree _tree;
	private ListenerManager<BaySelectionListener> _lm =
			new ListenerManager<BaySelectionListener>(BaySelectionListener.class, new AWTDispatcher<BaySelectionListener>()); 
	
	private SFPDevice _selDev = null;
	private int       _selBay = -1;
	
	private static class TreeRenderer extends DefaultTreeCellRenderer {

		private Icon connectedIcon = Resources.getIcon("connect");
		private Icon disconnectedIcon = Resources.getIcon("disconnect");
		
		
		private TreeRenderer() 
		{
			setOpenIcon(Resources.getIcon("pcb16"));
			setClosedIcon(Resources.getIcon("pcb16"));
			setLeafIcon(disconnectedIcon);
		}

		
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			
			if (value instanceof DeviceBayNode) {
				DeviceBayNode dbn = DeviceBayNode.class.cast(value);
				SFPDevice dev = DeviceNode.class.cast(dbn.getParent()).getDevice();
				if (dev.isModulePresent(dbn.pos)) {
					setLeafIcon(connectedIcon);
				} else {
					setLeafIcon(disconnectedIcon);
				}
			}
			
			return super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
		}
		
		
		
		
	}
	
	public DeviceManager(SFPManager mgr)
	{
		super();
		setLayout(new BorderLayout());
		_devMgrMod = new DeviceManagerModel(mgr);
		_tree = new JTree(_devMgrMod.getTreeModel());
		_tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		_tree.getSelectionModel().addTreeSelectionListener(this);
		_tree.setRootVisible(false);
		_tree.setRowHeight(-1);
		
		
		
		
		_tree.setCellRenderer(new TreeRenderer());
		
		
		_devMgrMod.getTreeModel().addTreeModelListener(this);

		JScrollPane sp = new JScrollPane(_tree);
		
		expandAll();
		add(new Title("Device Manager", Resources.getIcon("network-wired")), BorderLayout.NORTH);
		add(sp, BorderLayout.CENTER);
	}
	

	


	private void expandAll() {
	    int j = _tree.getRowCount();
	    int i = 0;
	    while(i < j) {
	        _tree.expandRow(i);
	        i += 1;
	        j = _tree.getRowCount();
	    }
	}
	
	public void addDeviceSelectedListener(BaySelectionListener dsl) {
		_lm.addListener(dsl);
	}
	
	private boolean _ignoreEvents = false;

	@Override
	public void valueChanged(TreeSelectionEvent tse) 
	{
		if (_ignoreEvents) return;
		
		Object[] objs= tse.getPath().getPath();
		if (objs.length != 3) {
			_ignoreEvents = true;
			_tree.setSelectionPath(tse.getOldLeadSelectionPath());
			_ignoreEvents = false;
			return;
		}
		
		SFPDevice d = DeviceNode.class.cast(objs[1]).getDevice();
		int pos = DeviceBayNode.class.cast(objs[2]).pos;
		
		
		for (BaySelectionListener dsl : _lm.getListeners()) {
			if (!dsl.canSelectBay(d, pos)) {
				_ignoreEvents = true;
				_tree.setSelectionPath(tse.getOldLeadSelectionPath());
				_ignoreEvents = false;
				return;
			}
		}
		
		if (_selDev != d) {
			if (_selDev != null) _selDev.removeDeviceListener(this);
			d.addDeviceListener(this);
		}
		
		_selDev = d;
		_selBay = pos;
		
		
		_lm.getProxy().baySelected(d, pos);
	}

	@Override
	public void sfpModuleStateChanged(SFPDevice dev, int bay) {

		
		
		if (dev != _selDev || bay != _selBay) return;
		
		_lm.getProxy().baySelected(dev, bay);
		repaint();
	}





	@Override
	public void treeNodesChanged(TreeModelEvent arg0) 
	{
		//expandAll();
		_tree.expandPath(arg0.getTreePath());
	}





	@Override
	public void treeNodesInserted(TreeModelEvent arg0) {
		expandAll();
	}





	@Override
	public void treeNodesRemoved(TreeModelEvent arg0) {
		//expandAll();
	}





	@Override
	public void treeStructureChanged(TreeModelEvent arg0) {
		expandAll();
	}

	


}
