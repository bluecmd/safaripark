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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import nl.nikhef.safaripark.AppContext;
import nl.nikhef.safaripark.ContextCache;
import nl.nikhef.safaripark.extra.GuiUtils;
import nl.nikhef.safaripark.extra.Module;
import nl.nikhef.safaripark.res.Resources;
import nl.nikhef.sfp.SFPDevice;
import nl.nikhef.sfp.ddmi.DDMI;
import nl.nikhef.sfp.ddmi.DDMIContext;
import nl.nikhef.sfp.ddmi.DDMIElement;
import nl.nikhef.sfp.ddmi.DDMIGroup;
import nl.nikhef.sfp.ddmi.DDMILoader;

@SuppressWarnings("serial")
public class EditPane extends JPanel {

	private DDMI _ddmi;
	private JTabbedPane _tab;
	private JToolBar    _tbar;
	private EditorContext _ctx;
	private AppContext _appCtx;
	private Module _module;

	private Action _apply = new AbstractAction("Apply", Resources.getIcon("dialog-apply")) {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			_ctx.commit();
			_ctx.getContext().updateChecksums();
			//_dev.refreshInfo(_bay);
			_module.dev.refreshInfo(_module.bay);
		}
	};

	
	private Action _revert = new AbstractAction("Revert", Resources.getIcon("dialog-cancel")) {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
			if (JOptionPane.showConfirmDialog(EditPane.this, "Discard changes?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) 
			{
				_ctx.revert();
			}
		}
	};
	

	private Action _reload = new AbstractAction("Reload", Resources.getIcon("arrow-refresh")) {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			_ctx.reload();
		}
	};
	
	
	public EditPane(AppContext appCtx) {
		
		
		
		setLayout(new BorderLayout());

		
		_appCtx = appCtx;
		_ctx = new EditorContext(_appCtx, _apply, _revert, _reload);
		
		
		_tbar = new JToolBar("Form Editor");
		_tbar.setFloatable(false);
		
		_apply.setEnabled(false);
		_revert.setEnabled(false);
		_reload.setEnabled(false);
		//_tbar.set
		
		
		_tbar.add(_apply);
		_tbar.add(_revert);
		_tbar.add(_reload);
		
		
		GuiUtils.toolbarTextButtions(_tbar);
		
		add(_tbar, BorderLayout.NORTH);
		_tab = new JTabbedPane();
		add(_tab, BorderLayout.CENTER);
		
		
	}
	
	public void initEditor(DDMILoader loader)
	{
		_ddmi = loader.getDDMI();
		createTabs(_ddmi);			
	}
	
	
	public EditorContext getContext() {
		return _ctx;
	}
		
	public synchronized void populate(SFPDevice dev, int bay) throws IOException
	{
		_apply.setEnabled(false);
		_revert.setEnabled(false);

		if (dev.isModulePresent(bay)) {
			DDMIContext ctx = _appCtx.ctxCache.getContextFor(dev, bay);
			_module = new Module(dev, bay);
			_ctx.setContext(ctx, _module);
			_reload.setEnabled(true);
			
		} else {
			_module = null;
			_ctx.setContext(null, null);
			_reload.setEnabled(false);
			
		}
		
	}
	
	


	
	
	private void createTabs(DDMIGroup ddmi) 
	{
		for (DDMIElement el : ddmi.getChildren()) {
			if (!(el instanceof DDMIGroup)) continue;
			DDMIGroup grp = DDMIGroup.class.cast(el);
			if (grp.getLabel() == null) {
				createTabs(grp);
			} else {
				JScrollPane sp = new JScrollPane(new FormEditPane(_ctx, grp, 0));
				sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
				_tab.addTab(grp.getLabel(), sp);
			}
			
		}
	}


	
	
}
