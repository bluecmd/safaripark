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
package nl.nikhef.safaripark.vsp;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.ItemSelectable;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

import nl.nikhef.safaripark.Config;
import nl.nikhef.safaripark.extra.ExtendedAbstractAction;
import nl.nikhef.safaripark.res.Resources;
import nl.nikhef.sfp.ddmi.DDMI;
import nl.nikhef.sfp.ddmi.DDMIElement;
import nl.nikhef.sfp.ddmi.DDMIGroup;
import nl.nikhef.sfp.ddmi.DDMILoader;
import nl.nikhef.sfp.ddmi.DDMIValue;
import nl.nikhef.tools.Filter;
import nl.nikhef.tools.ListenerManager;

public class ValueSelectionPane extends JPanel implements ItemListener, ItemSelectable {

	private JTree tree = new JTree();
	// private static final Dimension MIN_SIZE = new Dimension(400, 400); 
	
	private CheckableTreeNode _root;
	private DDMI			  _ddmi;
	private Preferences		  _presets;
	private DefaultComboBoxModel<String> _presetCbm;
	private JComboBox<String> _presetCb;
	private Map<String, CheckableTreeNode> _pathMapping = new HashMap<String, CheckableTreeNode>();
	private boolean			  _ignorePresetEvents = false;
	private ListenerManager<ItemListener> _ilmgr = new ListenerManager<ItemListener>(ItemListener.class);
	private boolean 		  _ignoreCheckEvents = false;
	
	
	@SuppressWarnings("serial")
	private Action _saveAction = new ExtendedAbstractAction("Save", Resources.getIcon("disk"), "Save preset") {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			String name = null;
			if (_presetCb.getSelectedIndex() == 0) 
			{
				boolean allOk = false;

				while (!allOk) {
				
					name = JOptionPane.showInputDialog(ValueSelectionPane.this, "Provide name for new preset", "Save preset", JOptionPane.INFORMATION_MESSAGE);
					allOk = true;
					if (name == null) {
						return;										
					}
					
					if (_presetCbm.getIndexOf(name) != -1) {
						JOptionPane.showMessageDialog(ValueSelectionPane.this, "That preset already exists");
						allOk = false;
						continue;
					}
					if (name.indexOf('<') >= 0 || name.indexOf('>') >= 0 || name.indexOf('/') >= 0) {
						JOptionPane.showMessageDialog(ValueSelectionPane.this, "Name can not contain < > and /");
						allOk = false;
						continue;
						
					}
				}
				
				_ignorePresetEvents = true;
				_presetCbm.addElement(name);
				_presetCb.setSelectedItem(name);
				_ignorePresetEvents = false;
			} else {
				name = String.class.cast(_presetCb.getSelectedItem());
				
				if (JOptionPane.showConfirmDialog(ValueSelectionPane.this, "Overwrite preset " + name + "?", "Confirm", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
					return;
				}
			}
			
			savePreset(name);
			updateActions();
		}

		
	};

	@SuppressWarnings("serial")
	private Action _deleteAction = new ExtendedAbstractAction("Delete", Resources.getIcon("bin-delete"),"Delete current preset") {

		@Override
		public void actionPerformed(ActionEvent e) {
			
			// this should not happen, but just in case
			if (_presetCb.getSelectedIndex() == 0) return;

			_ignorePresetEvents = true;
			
			deletePreset(String.class.cast(_presetCb.getSelectedItem()));
			_presetCbm.removeElement(_presetCb.getSelectedItem());
			_presetCb.setSelectedIndex(0);
			_ignorePresetEvents = false;
			
			updateActions();
		}
		
	};
	
	
	@SuppressWarnings("serial")
	public ValueSelectionPane(DDMILoader loader) {
		setLayout(new BorderLayout());
		CheckBoxNodeRenderer renderer = new CheckBoxNodeRenderer();
	    tree.setCellRenderer(renderer);

	    tree.setCellEditor(new CheckBoxNodeEditor(tree));
	    tree.setEditable(true);
	    tree.setShowsRootHandles(true);
	    tree.setRootVisible(false);
	    
	    
	    JToolBar jtb = new JToolBar();
	    jtb.setFloatable(false);
	    jtb.add(new JLabel(" Preset "));
	    _presetCb = new JComboBox<String>(new String[] { "<Initializing...>" } );
	    _presetCb.addItemListener(this);
	    jtb.add(_presetCb);
	    jtb.add(_saveAction);
	    jtb.add(_deleteAction);
	    
	    add(jtb, BorderLayout.SOUTH);
	    
	    JScrollPane scrollPane = new JScrollPane(tree);
	    add(scrollPane, BorderLayout.CENTER);
	    //setMinimumSize(MIN_SIZE);
	    //setPreferredSize(MIN_SIZE);
		_ddmi = loader.getDDMI();
		_root = new CheckableTreeNode(null, _ddmi, "DDMI") {
			protected void checkStateChanged() {
				ValueSelectionPane.this.checkStateChanged();
			};
		};
	}
	

	protected void checkStateChanged() {
		if (_ignoreCheckEvents) return;
		// XXX nasty hack! Should have a proper event object
		_ilmgr.getProxy().itemStateChanged(null);
		
	}


	private void savePreset(String name) {
		
		Preferences p = _presets.node(name);
		for (Map.Entry<String, CheckableTreeNode> entry : _pathMapping.entrySet())
		{
			if (entry.getValue().isChecked()) 
			{
				p.putBoolean(entry.getKey(), true);		
			}
		}
		try {
			_presets.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}
	
	private void deletePreset(String name) {
		Preferences p = _presets.node(name);
		try {
			p.removeNode();
			_presets.flush();
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}
	}

	
	private void loadPreset(String name) {
		_ignoreCheckEvents = true;
		Preferences p = _presets.node(name);
		for (Map.Entry<String, CheckableTreeNode> entry : _pathMapping.entrySet())
		{
			
			boolean checked = p.getBoolean(entry.getKey(), false);
			if (checked) {
				entry.getValue().check();
			} else {
				entry.getValue().uncheck();
			}
			
		}
		//DefaultTreeModel.class.cast(tree.getModel())
		tree.repaint();
		_ignoreCheckEvents = false;
		checkStateChanged();
	}

	private int fillTree(String path, CheckableTreeNode root, DDMIGroup group, Filter<DDMIElement> ddmiFilter)
	{
		int c = 0;
		
		for (DDMIElement e : group.getChildren())
		{
			if (e instanceof DDMIGroup && e.getLabel() == null) 
			{
				c += fillTree(path, root, DDMIGroup.class.cast(e), ddmiFilter);
				continue;
			}
			
			CheckableTreeNode dmt = new CheckableTreeNode(root, e, e.getLabel());					
			
			String ePath;

			if (path == null) {
				ePath = e.getName();
			} else {
				ePath = path + "." + e.getName();
			}

			if (e instanceof DDMIGroup) {
				
				
				
				if (fillTree(ePath, dmt, DDMIGroup.class.cast(e), ddmiFilter) > 0) {
					root.add(dmt);	
					c++;
				}
			} else if (e instanceof DDMIValue) {
				
				if (ddmiFilter != null && !ddmiFilter.match(e)) continue;
				
				root.add(dmt);
				
				_pathMapping.put(ePath, dmt);
				
				c++;
				
			}
		}
		return c;
	}
	
	public void init(Filter<DDMIElement> ddmiFilter, String name)
	{
		
		_ignoreCheckEvents = true;
		_root.removeAllChildren();
		_pathMapping.clear();
		fillTree(null, _root, _ddmi, ddmiFilter);
	
		tree.setModel(new DefaultTreeModel(_root));
		
		Preferences p = Config.PREFS.node("vsp");
		_presets = p.node(name);
		
		Vector<String> options = new Vector<String>();
		options.add("<new preset>");
		
		try {
			options.addAll(Arrays.asList(_presets.childrenNames()));
		} catch (BackingStoreException e) {
			e.printStackTrace();
		}

		_presetCbm = new DefaultComboBoxModel<String>(options);
		
		_ignorePresetEvents = true;
		_presetCb.setModel(_presetCbm);
		_ignorePresetEvents = false;
		updateActions();
		_ignoreCheckEvents = false;
		checkStateChanged();
	}
	
	
	
	
	
	
	private void updateActions() {
		if (_presetCb.getSelectedIndex() == 0) {
			_deleteAction.setEnabled(false);
		} else {
			_deleteAction.setEnabled(true);
		}
	}


	public List<DDMIValue>  getSelection()
	{
		List<DDMIValue> values = new ArrayList<DDMIValue>();
		
		crawlAddSelected(_root, values);
	
		return values;
	}
	
	private void crawlAddSelected(CheckableTreeNode tn, List<DDMIValue> values) 
	{
		for (int i = 0; i < tn.getChildCount(); i++)
		{
			CheckableTreeNode child = CheckableTreeNode.class.cast(tn.getChildAt(i));
			if (child.isLeaf()) {
				if (child.getState() == TristateCheckbox.CHECKED) {
					values.add(DDMIValue.class.cast(child.getUserObject()));
				}
			} else {
				crawlAddSelected(child, values);	
			}
		}
	}
	private void crawlSetSelected(CheckableTreeNode tn, Collection<DDMIValue> values) 
	{
		for (int i = 0; i < tn.getChildCount(); i++)
		{
			CheckableTreeNode child = CheckableTreeNode.class.cast(tn.getChildAt(i));
			if (child.isLeaf()) {
				if (values.contains(child.getUserObject())) {
					child.check();				
				} else {
					child.uncheck();
				}
			} else {
				crawlSetSelected(child, values);	
			}
		}
	}


	
	static ValueSelectionPane vsp;
	
	public static void main(String[] args) throws IOException, XMLStreamException, SAXException {
		
		try {
			UIManager.setLookAndFeel(
			            UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		} catch (UnsupportedLookAndFeelException e) {
		}

		
		
		JFrame jf = new JFrame("Blah");
		jf.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		jf.addWindowListener(new WindowListener() {
			
			@Override
			public void windowOpened(WindowEvent e) {
			}
			
			@Override
			public void windowIconified(WindowEvent e) {
			}
			
			@Override
			public void windowDeiconified(WindowEvent e) {
			}
			
			@Override
			public void windowDeactivated(WindowEvent e) {
			}
			
			@Override
			public void windowClosing(WindowEvent e) {
				// System.out.printf("%d element(s) selected\n", vsp.getSelection().size());
			}
			
			@Override
			public void windowClosed(WindowEvent e) {
			}
			
			@Override
			public void windowActivated(WindowEvent e) {
			}
		});
		
		DDMILoader loader = new DDMILoader();
		
		vsp = new ValueSelectionPane(loader);
		vsp.init(new Filter<DDMIElement>() {

			@Override
			public boolean match(DDMIElement other) {
				if (other instanceof DDMIValue) {
					DDMIValue val = DDMIValue.class.cast(other);
					if (val.isWritable()) {
						return true;
					}
				}
				
				return false;
			}

			@Override
			public Class<DDMIElement> getFilterClass() {
				return DDMIElement.class;
			}
			
		}, "test");
		jf.getContentPane().add(vsp, BorderLayout.CENTER);
		jf.pack();
		
		jf.setVisible(true);
	}


	@Override
	public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() != ItemEvent.SELECTED) return;
		if (_ignorePresetEvents) return;
		if (_presetCb.getSelectedIndex() == 0) return;
		loadPreset(String.class.cast(_presetCb.getSelectedItem()));
		updateActions();
	}


	public boolean showModel(JComponent parent) {
		
		return JOptionPane.showOptionDialog(parent, new JComponent[] { this }, "Select elements", JOptionPane.OK_CANCEL_OPTION, JOptionPane.INFORMATION_MESSAGE,
				null, null, null) == JOptionPane.OK_OPTION;		
	}


	public void setSelection(List<DDMIValue> columns) {
		crawlSetSelected(_root, columns);
	}


	@Override
	public Object[] getSelectedObjects() {
		return getSelection().toArray();
	}


	@Override
	
	public void addItemListener(ItemListener l) {
		_ilmgr.addListener(l);
	}


	@Override
	public void removeItemListener(ItemListener l) {
		_ilmgr.removeListener(l);
	}

}
