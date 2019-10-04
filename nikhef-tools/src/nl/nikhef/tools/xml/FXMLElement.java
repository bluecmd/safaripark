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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FXMLElement {

	private Map<String, String> _att;
	private List<FXMLElement>	_children;
	private String				_chars;
	
	public final String tag;
	
	public FXMLElement(String tag) {
		this.tag = tag;
	}

	public void setAttribute(String attName, String attValue)
	{
		if (_att == null) _att = new HashMap<String, String>();
		_att.put(attName, attValue);
	}

	public void setChars(String chars) 
	{
		if (_children != null) throw new RuntimeException("Mixing children/cdata not allowed");
		_chars = chars;
	}

	public void addChild(FXMLElement child) {
		if (_chars != null) throw new RuntimeException("Mixing children/cdata not allowed");
		if (_children == null) _children = new ArrayList<FXMLElement>();
		_children.add(child);
	}
	
	public String getAttibute(String name) 
	{
		if (_att == null) return null;
		return _att.get(name);
	}

	
	// pretty print stuff
	
	private void mkPadding(int c)
	{
		if (c == 0) return;
		char[] chars = new char[c];
		Arrays.fill(chars, ' ');
		
		System.out.print(new String(chars));
	}
	
	private String att2Str()
	{
		if (_att == null || _att.size() == 0) return "";
		
		StringBuilder sb = new StringBuilder();
		
		for (Map.Entry<String, String> kv : _att.entrySet())
		{
			sb.append(' ');
			sb.append(kv.getKey());
			sb.append("=\"");
			sb.append(kv.getValue());
			sb.append('"');
		}
		return sb.toString();
	}
	
	private void prettyPrint(int indent)
	{
		if (_children != null) {
			mkPadding(indent);
			System.out.printf("<%s>\n", tag);
			
			for (FXMLElement e : _children)
			{
				e.prettyPrint(indent + 1);
			}
			
			mkPadding(indent);
			System.out.printf("</%s>\n", tag);
		} else if (_chars != null) {
			mkPadding(indent);
			System.out.printf("<%s%s>%s</%s>\n", tag, att2Str(), _chars, tag);
		} else {
			mkPadding(indent);
			System.out.printf("<%s%s/>\n", tag, att2Str());
		}
	}
	
	public void prettyPrint() {
		prettyPrint(0);
	}

	public List<FXMLElement> children() {
		if (_children == null) return Collections.emptyList();
		return _children;
	}

	public boolean hasAttribute(String name) {
		if (_att == null) return false;
		return _att.containsKey(name);
	}
	
	
	@Override
	public String toString() {
		return String.format("<%s%s/>", tag, att2Str());
	}

	public Map<String, String> attributes() {
		if (_att == null) return Collections.emptyMap();
		return Collections.unmodifiableMap(_att);
	}

	public boolean hasChars() {
		return _chars != null;
	}
	
	public boolean hasChildren() {
		return _children != null && _children.size() > 0;
	}

	public String getChars() {
		return _chars;
		
	}

	public FXMLElement getChildByName(String name) {
		for (FXMLElement child : _children) {
			if (child.hasAttribute("name") && child.getAttibute("name").equals(name)) {
				return child;
			}
		}
						
		return null;
	}
}
