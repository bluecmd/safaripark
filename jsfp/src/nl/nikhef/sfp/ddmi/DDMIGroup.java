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

import java.util.ArrayList;
import java.util.List;

public class DDMIGroup extends DDMIElement {

	
	private List<DDMIElement> _children = new ArrayList<DDMIElement>();	

	
	public void add(DDMIElement child) {
		_children.add(child);
	}
	
	
	public List<DDMIElement> getChildren() {
		return _children;
	}
	
	@Override
	public void output(StringBuilder sb, DDMIContext ctx, int level) 
	{
		SFPUtils.pad(sb, level);
		
		if (getLabel() != null) {
			sb.append(String.format("@ Group label=%s\n", getLabel()));
		} else {
			sb.append(String.format("@ Group hash=%08x\n", this.hashCode()));
		}
		
		
		for (DDMIElement child : _children) {
			if (child.isValid(ctx)) {
				child.output(sb, ctx, level + 1);
			}
		}
	}

	protected DDMIElement findElementForId(String id) 
	{
		for (DDMIElement element : getChildren()) 
		{
			if (id.equals(element.getId())) {
				return element;
			}
			if (element instanceof DDMIGroup) {
				DDMIElement el = DDMIGroup.class.cast(element).findElementForId(id);
				if (el != null) return el;
			} 
		}
		return null;
	}

	/**
	 * Searches the element tree for matching elements for the specified filter.
	 * 
	 * @param filter	The filter to apply for the search
	 * @param list		The list to add the matched elements to
	 */
	protected <T extends DDMIElement> void findElements(DDMIFilter<T> filter, List<T> list) 
	{
		for (DDMIElement element : getChildren()) 
		{
			
			if (element instanceof DDMIGroup) 
			{
				DDMIGroup.class.cast(element).findElements(filter, list);
			} 

			if (!filter.matchClass().isAssignableFrom(element.getClass())) continue;
			
			T match = filter.matchClass().cast(element);
			
			if (filter.matches(match)) list.add(match);
		}
	}
	
	
}
