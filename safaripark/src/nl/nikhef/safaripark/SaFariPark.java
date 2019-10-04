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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.SplashScreen;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.prefs.Preferences;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

import nl.nikhef.safaripark.devmgr.BaySelectionListener;
import nl.nikhef.safaripark.devmgr.DeviceManager;
import nl.nikhef.safaripark.dolly.DollyModel;
import nl.nikhef.safaripark.dolly.DollyPanel;
import nl.nikhef.safaripark.editpane.EditPane;
import nl.nikhef.safaripark.extra.BaseMessage;
import nl.nikhef.safaripark.extra.ExtendedAbstractAction;
import nl.nikhef.safaripark.extra.GuiUtils;
import nl.nikhef.safaripark.extra.Message;
import nl.nikhef.safaripark.extra.MessagePane;
import nl.nikhef.safaripark.extra.StatefulTask;
import nl.nikhef.safaripark.monitor.Monitor;
import nl.nikhef.safaripark.res.Resources;
import nl.nikhef.sfp.SFPDevice;
import nl.nikhef.sfp.SFPManager;
import nl.nikhef.sfp.ddmi.DDMILoader;

@SuppressWarnings("serial")
public class SaFariPark extends JFrame implements BaySelectionListener, WindowListener, DeferredListener {

	
	private static final int SCAN_DEVICE_DELAY = 2;
	
	private static final Logger LOG = Logger.getLogger(SaFariPark.class.getSimpleName());
	
	private DeviceManager 	_devMgr;
	private EditPane   		_tabEdit;
	private ActiveLogArea   _logOut;
	private Monitor			_monitor;
	private StatusBar       _status;
	private Timer			_timer;
	private int  			_scanForNewDevices = SCAN_DEVICE_DELAY;
	private AppContext      _appCtx;
	private JSplitPane		_topSplit;
	private JSplitPane		_rightSplit;
	private JSplitPane		_leftSplit;
	private MessagePane		_messagePane = new MessagePane();
	
	private ModuleManager   _modMgr;
	private DollyPanel		_dp;
	private static Properties		buildProps;
	
	private Action _selectOverlays = new ExtendedAbstractAction("Select overlays", Resources.getIcon("books"), "Select which SFP+ overlays to load") {
		
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			
			_appCtx.ovlMgr.showDialog(SaFariPark.this);
		}
	};
	
	private Action _saveBinary = new ExtendedAbstractAction("Export binary data", Resources.getIcon("document-export-4"), "Save a binary export") {
		
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			_modMgr.exportBinary(SaFariPark.this);
		}
	};
	
	private Action _backupBinary = new ExtendedAbstractAction("Backup module", Resources.getIcon("database-go"), "Make a backup module's content") {
		
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			_modMgr.backupBinary(SaFariPark.this);
		}
	};

	private Action _restoreBinary = new ExtendedAbstractAction("Restore backup", Resources.getIcon("database-refresh"), "Restore a backup of module's content") {
		
		@Override
		public void actionPerformed(ActionEvent e) 
		{
			_modMgr.restoreBinary(SaFariPark.this);
		}
	};

	
	private Message _msg;
	
	private Action _help = new ExtendedAbstractAction("Help", Resources.getIcon("help"), "Loads the manual") {
		
		@Override
		public void actionPerformed(ActionEvent e) {
			File f = new File("multisfp.pdf");
			if (Desktop.isDesktopSupported()) {
			    try {
			        Desktop.getDesktop().open(f);
			        f = null;
			    } catch (Exception ex) {
			    	
			    }
			}
			if (f != null) {
				if (_msg == null) {
					 _msg = new BaseMessage("Help can not be opened, please open manually at " + f.getAbsolutePath());
				}
				_appCtx.getMessagePane().addMessage(_msg);
			}
		}
	};

	private StatefulTask _task;
	
	private Preferences _guiPrefs;
	
	
	

	
	

	public SaFariPark(AppContext appCtx) throws IOException, XMLStreamException 
	{
		
		_appCtx = appCtx;
		setLayout(new BorderLayout());
		
		setTitle("SaFariPark " + getFullRevision());
		setSize(640, 480);
		setIconImage(Resources.getImage("appicon"));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		
		_devMgr = new DeviceManager(appCtx.sfpMgr);
		_devMgr.addDeviceSelectedListener(this);
		
		_tabEdit = new EditPane(appCtx);
		_logOut = new ActiveLogArea();
		_monitor = new Monitor(appCtx);
		_status = new StatusBar();
		_modMgr = new ModuleManager(_saveBinary, _backupBinary, _restoreBinary, appCtx);
		_devMgr.addDeviceSelectedListener(_modMgr);
		_appCtx.setMessagePane(_messagePane);
		_appCtx.setSaFariPark(this);
		
		DollyModel dm = new DollyModel(appCtx);
		appCtx.sfpMgr.addSFPProviderListener(dm);
		_dp = new DollyPanel(dm, appCtx);
		makeToolbar();
		_topSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		_topSplit.setDividerLocation(300);
		
		_leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		_leftSplit.setTopComponent(_devMgr);
		_leftSplit.setBottomComponent(_dp);
		_leftSplit.setDividerLocation(300);
		_topSplit.setLeftComponent(_leftSplit);
		
		_rightSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		_topSplit.setRightComponent(_rightSplit);
		_rightSplit.setTopComponent(_tabEdit);
		_rightSplit.setResizeWeight(0.9);

		_guiPrefs = Config.PREFS.node("gui");
		
		JTabbedPane tp = new JTabbedPane();
		tp.addTab("Monitor", _monitor);
		tp.addTab("Logging", _logOut);
		
		_rightSplit.setBottomComponent(tp);
		
		JPanel content = new JPanel();
		content.setLayout(new BorderLayout());
		content.add(_messagePane, BorderLayout.NORTH);
		content.add(_topSplit, BorderLayout.CENTER);
		add(content, BorderLayout.CENTER);
		add(_status, BorderLayout.SOUTH);
		// setExtendedState(JFrame.MAXIMIZED_BOTH);
		addWindowListener(this);
		
		
		_timer = new Timer(1000, new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (_scanForNewDevices == 0) { 
					_appCtx.sfpMgr.update(true);
					_scanForNewDevices = SCAN_DEVICE_DELAY;
				} else {
					_appCtx.sfpMgr.update(false);
					_scanForNewDevices--;
				}
				
			}
		});
		_timer.setRepeats(true);
	}
	
	
	
	private static  void loadBuildProps() {
		buildProps = new Properties();
		try {
			InputStream is = SaFariPark.class.getResourceAsStream("build.properties");
			if (is != null) 
			{
				buildProps.load(is);
				return;
			}
		} catch (IOException e) {
		}	
		buildProps = null;
	}



	private static String getFullRevision() {
		
		if (buildProps == null) {
			return "(development)";
		}
		
		return buildProps.getProperty("git.tag") + "-" + buildProps.getProperty("git.revision");
	}



	private void makeToolbar() 
	{
		JToolBar tb = new JToolBar();
		tb.setFloatable(false);
		tb.add(_selectOverlays);
		tb.add(_saveBinary);
		tb.addSeparator();
		tb.add(_backupBinary);
		tb.add(_restoreBinary);
		tb.add(Box.createHorizontalGlue());
		tb.add(_help);
		
		GuiUtils.toolbarTextButtions(tb);
		add(tb, BorderLayout.NORTH);
	}
	
	
	
	private Runnable _taskRunner = new Runnable() {

		@Override
		public void run() 
		{
			if (_task.execute()) {
				_task = null;
				_status.setProgress(0, 100);
			} else {
				_status.setProgress(_task.getProgress(), 100);
				SwingUtilities.invokeLater(this);
			}
		}
		
	};
	
	public void executeTask(StatefulTask task)
	{
		if (_task != null) return;
		_task = task;
		SwingUtilities.invokeLater(_taskRunner);
	}
	
	public static void updateSplash(String str)
	{
		SplashScreen ss = SplashScreen.getSplashScreen();
		// splash may not be required
		if (ss == null) return;
		int height = ss.getBounds().height;
		int width = ss.getBounds().width;
		Graphics2D g2d = ss.createGraphics();
		g2d.setColor(Color.BLACK);
		g2d.fillRect(0, height - 20, width, 20);
		g2d.setColor(Color.WHITE);
		g2d.drawString(str, 10, ss.getBounds().height - 10);
		g2d.dispose();
		ss.update();
	}
	
	public static void main(String[] args) 
	{
		SaFariPark.loadBuildProps();
		Locale.setDefault(Locale.US);
		
		try {
			UIManager.setLookAndFeel(
			            UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
		} catch (InstantiationException e) {
		} catch (IllegalAccessException e) {
		} catch (UnsupportedLookAndFeelException e) {
		}

		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
		    public void uncaughtException(Thread t, Throwable e) {
		    	SaFariPark.showFatalError(e, null);
		    }
		});
		
		
		LogManager.getLogManager().getLogger("").setLevel(Level.INFO);
		
		Logger globalLogger = Logger.getLogger("");
		Handler[] handlers = globalLogger.getHandlers();
		for(Handler handler : handlers) {
		    globalLogger.removeHandler(handler);
		}
		


		if (args.length == 2 && args[0].equals("--setdpi")){
			
			
			if (args[1].equals("auto")) {
				setDpi(-1);
			} else {
				setDpi(Integer.parseInt(args[1]));
			}
		}
		
		
		
		SaFariPark sfpE;
		try {
			updateSplash("Initializing SFP drivers");
			SFPManager sfpMgr = new SFPManager();
			updateSplash("Loading XML definitions");
			DDMILoader ddmiLdr = new DDMILoader();
			OverlayManager ovlMgr = new OverlayManager();
			ovlMgr.scanDirectory(new File("overlays"));
			ddmiLdr.loadOverlays(ovlMgr.getOverlays());
			
			
			AppContext appCtx = new AppContext(sfpMgr, ddmiLdr, ovlMgr);
			
			sfpE = new SaFariPark(appCtx);
			sfpE.setVisible(true);
			sfpE.init();
		} catch (IOException e) {
			showFatalError(e, null);
		} catch (XMLStreamException e) {
			showFatalError(e, null);
		} catch (SAXException e) {
			showFatalError(e, null);
		}
		
	}
				 
	
	private static void showFatalError(Throwable e, Component parent) {
		
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		
		pw.println("Oh no! An error occured.");
		pw.println("If the problem persists, please copy the information below");
		pw.println("and create a new issue at:");
		pw.println("  http://www.ohwr.org/projects/sfp-plus-i2c/issues");
		pw.println();
		pw.printf("%s Java %s (%s bit) and %s (OS=%s, %s)\n",
				System.getProperty("java.vendor"),
				System.getProperty("java.version"),
				System.getProperty("sun.arch.data.model") ,
				System.getProperty("os.name"),
				System.getProperty("os.version"),
				System.getProperty("os.arch"));
		pw.printf("SaFariPark=%s\n", SaFariPark.getFullRevision());

		pw.println("Error trace:");
		e.printStackTrace(pw);

		
		
		JTextArea jte = new JTextArea(sw.toString());
		JScrollPane jsp = new JScrollPane(jte);
		
		GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		int width = gd.getDisplayMode().getWidth();
		int height = gd.getDisplayMode().getHeight();
		
		Dimension dm = new Dimension(width / 2, height / 2);
		
		jte.setEditable(false);
		
		jsp.setPreferredSize(dm);
		JOptionPane.showMessageDialog(parent, jsp, "Fatal error", JOptionPane.ERROR_MESSAGE);
		
		System.exit(1);
	}



	public static void setDpi(int dpi) {
		
		
		if (dpi == -1) {
			dpi = Toolkit.getDefaultToolkit().getScreenResolution();
		}
		
		
		// no high dpi screen, exit.
		//if (dpi < 100) return;
		//dpi = 45;
		
		float scale = (float)dpi / 96f;
		
		UIDefaults d = UIManager.getLookAndFeel().getDefaults();
		
		
		
		for (Entry<Object, Object> x : d.entrySet() )
		{
			
			Object o = UIManager.get(x.getKey());
			if (o == null) continue;
			if ((o instanceof Font)) {
				Font f = Font.class.cast(o);
				
				UIManager.put(x.getKey(), f.deriveFont(f.getSize2D() * scale));
			}
			if (x.getKey().toString().equals("Tree.rowHeight")) {
				UIManager.put(x.getKey(), (int)( Integer.class.cast(o).intValue() * scale));
			}
		}
	}

	public void init() {
		
		//restoreGuiPref(this, "topSplit.dividerLocation");
		
		Rectangle win = getBounds();
		win.x = _guiPrefs.getInt("win_x", win.x);
		win.y = _guiPrefs.getInt("win_y", win.y);
		win.width = _guiPrefs.getInt("win_width", win.width);
		win.height = _guiPrefs.getInt("win_height", win.height);
		setBounds(win);
		
		setExtendedState(_guiPrefs.getInt("state", getExtendedState()));
		
		_topSplit.setDividerLocation(_guiPrefs.getInt("top_split", _topSplit.getDividerLocation()));
		_leftSplit.setDividerLocation(_guiPrefs.getInt("left_split", _leftSplit.getDividerLocation()));
		_rightSplit.setDividerLocation(_guiPrefs.getInt("right_split", _rightSplit.getDividerLocation()));
		
		
		SwingUtilities.invokeLater(new Runnable() 
		{
			
			@Override
			public void run() {
				_tabEdit.initEditor(_appCtx.ddmiLdr);
				_timer.start();
			}
		});

		
		LOG.info("SaFariPark " + SaFariPark.getFullRevision());

		LOG.info(String.format("%s Java %s (%s bit) and %s (OS=%s, %s)\n",
				System.getProperty("java.vendor"),
				System.getProperty("java.version"),
				System.getProperty("sun.arch.data.model") ,
				System.getProperty("os.name"),
				System.getProperty("os.version"),
				System.getProperty("os.arch")));

	}
	
	public void deinit() {
		_guiPrefs.putInt("left_split", _leftSplit.getDividerLocation());
		_guiPrefs.putInt("right_split", _rightSplit.getDividerLocation());
		_guiPrefs.putInt("top_split", _topSplit.getDividerLocation());
		Rectangle win = getBounds(); 
		_guiPrefs.putInt("win_x", win.x);
		_guiPrefs.putInt("win_y", win.y);
		_guiPrefs.putInt("win_width", win.width);
		_guiPrefs.putInt("win_height", win.height);
		_guiPrefs.putInt("state", getExtendedState());
		
		_timer.stop();
	}

	
	@Override
	public void baySelected(SFPDevice dev, int bay) {
		try {
			_tabEdit.populate(dev, bay);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean canSelectBay(SFPDevice dev, int bay) {
		if (_tabEdit.getContext().someAreDirty()) {
			if (JOptionPane.showConfirmDialog(this, 
					"Editor contains changes which are not yet applied\n"
					+ "Press OK to discard the changes and edit the selected device\n"
					+ "Press CANCEL to return to keep editing the current device", 
					"Warning", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.WARNING_MESSAGE) == JOptionPane.CANCEL_OPTION) {
				return false;
			}
		}
		return true;
	}






	@Override
	public void windowOpened(WindowEvent e) {
	}



	@Override
	public void windowClosing(WindowEvent e) {
		deinit();
	}



	@Override
	public void windowClosed(WindowEvent e) {
	}



	@Override
	public void windowIconified(WindowEvent e) {
	}


	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}



	private int _max = 0;
	
	@Override
	public synchronized void deferredTasksUpdated(int tasksQueued)
	{
		if (tasksQueued > _max) {
			_max = tasksQueued;
		}
		_status.setProgress(_max - tasksQueued, _max);
		//else _status.setDone();
	}

	

	
}
