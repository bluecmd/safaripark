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
package nl.nikhef.safaripark.extra;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public final class GuiUtils {

	private GuiUtils() {};
	
	public static final void toolbarTextButton(JButton button) 
	{
		button.setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
		button.setHideActionText(false);
		button.setHorizontalTextPosition(SwingConstants.TRAILING);
		button.setVerticalTextPosition(SwingConstants.CENTER);
	}

	public static final void toolbarTextButtions(JToolBar tb)
	{
		
		for (Component c: tb.getComponents())
		{
			if (c instanceof JButton) {
				toolbarTextButton(JButton.class.cast(c));
			}
		}
		
	}

	private static class StatefulRunner implements Runnable
	{
		
		private StatefulTask _task;

		public StatefulRunner(StatefulTask task) {
			_task = task;
		}

		@Override
		public void run() {
			if (_task.execute()) return;
			SwingUtilities.invokeLater(this);	
		}
				
		
	}
	
	public static void executeStatefulTask(final StatefulTask cloneTask) 
	{
		SwingUtilities.invokeLater(new StatefulRunner(cloneTask));		
	}
	
}
