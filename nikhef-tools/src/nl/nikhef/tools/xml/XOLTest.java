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
package nl.nikhef.tools.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

public class XOLTest {

	public static class Foo {
		
		private int _stuff;
		private List<Bar> _bars = new ArrayList<Bar>();
		
		public void setStuff(int stuff)
		{
			_stuff = stuff;			
		}
		
		public void add(Bar b) {
			_bars.add(b);
		}
		
		public String toString() {
			return String.format("{Foo stuff=%d bars=%s}", _stuff, _bars.toString());
		}
	}
	
	public static class Bar {
		
		
		private String _id;
		
		@Override
		public String toString() {
			return String.format("{Bar id=%s}", _id);
		}
		
		public void setId(String id) {
			_id = id;
		}
	}
	
	
	public static void main(String[] args) {
		
		XOL xol = new XOL();
		
		xol.setMapping("foo", Foo.class);
		xol.setMapping("bar", Bar.class);
		
		try {
			List<? extends Object> objs = xol.parseString(
					"<?xml version=\"1.0\"?>" +
					"<foo stuff=\"3\"><bar id=\"3\"/><bar id=\"4\"/></foo>");
			
			for (Object obj : objs) {
				System.out.println(obj.toString());
			}
		} catch (XMLStreamException e) {
			e.printStackTrace();
		}
		
	}
}

