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
package nl.nikhef.sfp.ddmi;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaValue;

public abstract class DDMIElement {

	private String _label;
	private String _id;
	private String _condition;
	private String _name;
	private String _qName;
	private DDMIElement _parent;
	
	
	private String _sourceId;
	private String 		_passwordId;
	


	public void setSourceId(String id)
	{
		_sourceId = id;
	}
	
	
	public String getSourceId() 
	{
		if (_sourceId == null) {
			DDMIElement parent = getParent();
			if (!(parent instanceof DDMIGroup))
				return null;
			
			return DDMIGroup.class.cast(parent).getSourceId();
		}
		return _sourceId;
	}

	public void setPasswordId(String id)
	{
		_passwordId = id;
	}
	
	
	public String getPasswordId() 
	{
		if (_passwordId == null) {
			DDMIElement parent = getParent();
			if (!(parent instanceof DDMIGroup))
				return null;
			
			return DDMIGroup.class.cast(parent).getPasswordId();
		}
		return _passwordId;
	}	
	
	
	public void setLabel(String label) {
		_label = label;		
	}
	
	public String getLabel() {
		return _label;
	}
	
	/**
	 * @return the id
	 */
	public String getId() {
		return _id;
	}
	
	public void setName(String name) {
		_name = name;
	}
	
	public String getName() {
		if (_name == null && _label != null) {
			label2Name();
		}
		return _name;
	}

	private void label2Name() {
		
		StringBuilder sb = new StringBuilder();
		
		for (char c : _label.toCharArray())
		{
			if (c == '-' || c == ' ' || c == '/' || c == '.' || c == '(' || c == ')') {
				sb.append('_');
			} else if ((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z')) 
			{				
				sb.append(c);
			} else if (c >='A' && c <='Z') {
				sb.append(Character.toLowerCase(c));
			}
		}
		// System.out.println("Created name " + sb.toString());
		_name = sb.toString();
	}


	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		_id = id;
	}
	
	public void setParent(DDMIElement parent) 
	{
		_parent = parent;
	}
	
	public DDMIElement getParent() {
		return _parent;
	}
	
	
	
	

	public void setShowIf(String condition) 
	{
		_condition = condition;
	}
	
	public boolean isValid(DDMIContext ctx) {
		if (_parent != null) {
			if (!_parent.isValid(ctx)) return false;
		}
		
		if (ctx.getI2CLink() == null) return false;
		
		if (_condition == null) {
			return true;
		}

		Globals globals = ctx.getGlobals();
		
		LuaValue func = globals.load("return " + _condition);		
		LuaValue val =  func.call();
		boolean b=  val.checkboolean();
		return b;
	}
	
	public abstract void output(StringBuilder sb, DDMIContext ctx, int level);

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((_label == null) ? 0 : _label.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DDMIElement other = (DDMIElement) obj;
		if (_label == null) {
			if (other._label != null)
				return false;
		} else if (!_label.equals(other._label))
			return false;
		return true;
	}

	
	
	public String getQualifiedName() {
		
		if (_qName == null) {
			makeQName();
		}

		return _qName;
	}


	private void makeQName() {
		if (_parent != null) {
			_qName = _parent.getQualifiedName();
		} else {
			_qName = null;
		}
		
		if (_qName == null) {
			_qName = getName();
		} else if (getName() != null) {
			_qName = _qName + "." + getName();			
		}

	}
	
}


