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
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class StatusBar extends JPanel {

	private JLabel _status;
	private JProgressBar _pb;
	private int _prgs;
	private int _max;
	private volatile boolean _prgsUdPending = false;
	
	private SimpleFormatter _format = new SimpleFormatter();
	
	public StatusBar() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEtchedBorder());
		_status = new JLabel(" ");
		add(_status, BorderLayout.CENTER);
		_pb = new JProgressBar();
		add(_pb, BorderLayout.LINE_END);
		_pb.setEnabled(false);
		
		
		loggerAsStatus();

	}
	
	
	
	
	
	private void loggerAsStatus() {
		Logger gLogger = Logger.getLogger("");
		gLogger.addHandler(new Handler() {
			
			@Override
			public void publish(final LogRecord record) {
				
				if (SwingUtilities.isEventDispatchThread()) {
	            	if (record.getLevel().intValue() < Level.INFO.intValue()) return;		            	
	            	
	                String txt = _format.formatMessage(record);
	                
	                setStatus(txt);
				} else {
			        SwingUtilities.invokeLater(new Runnable() {
	
			            @Override
			            public void run() {
			            	
			            	if (record.getLevel().intValue() < Level.INFO.intValue()) return;		            	
			            	
			                String txt = _format.formatMessage(record);
			                
			                setStatus(txt);
			            }
	
			        });
				}
		 
			}
			
			@Override
			public void flush() {
			}
			
			@Override
			public void close() throws SecurityException {
			}
		});
		
	}





	private synchronized void updateProgress() {
		if (!_prgsUdPending) return;
		if (_max == -1) {
			if (!_pb.isEnabled()) _pb.setEnabled(true);
			if (!_pb.isIndeterminate()) _pb.setIndeterminate(true);
		} else {
			if (_pb.isIndeterminate()) _pb.setIndeterminate(false);	
			if (_pb.getMaximum() != _max) _pb.setMaximum(_max);
			if (_pb.getValue() != _prgs) _pb.setValue(_prgs);
			if (_prgs == _max) {
				// TODO clear it later
			}
		}
		_prgsUdPending = false;
	}
	
	private void triggerUpdate() {
		if (_prgsUdPending) return;
		_prgsUdPending = true;
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				updateProgress();
			}
			
		});
	}
	
	public synchronized void setProgress(int current, int max)
	{
		_max = max;
		_prgs = current;
		triggerUpdate();
	}
	
	public synchronized void setIndeterminte()
	{
		_max = -1;
		triggerUpdate();
	}
	
	public synchronized void setDone()
	{
		_max = 1;
		_prgs = 1;
		triggerUpdate();
	}

	public void setStatus(String text) 
	{
		_status.setText(text);
	}

	
}
