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

import java.awt.TextArea;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class ActiveLogArea extends TextArea {
	
	private SimpleFormatter _format = new SimpleFormatter();
	
	public ActiveLogArea() {
		
		setEditable(false);
		
		Logger gLogger = Logger.getLogger("");
		gLogger.addHandler(new Handler() {
			
			@Override
			public void publish(final LogRecord record) {
				
				if (SwingUtilities.isEventDispatchThread()) {
	                String txt = String.format("[%s] %s (%s)\n", record.getLevel(), _format.formatMessage(record), record.getLoggerName());
	                append(txt);
				} else {
				
			        SwingUtilities.invokeLater(new Runnable() {
	
			            @Override
			            public void run() {
			                String txt = String.format("[%s] %s (%s)\n", record.getLevel(), _format.formatMessage(record), record.getLoggerName());
			                append(txt);
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
	

}
