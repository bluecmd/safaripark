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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class DDMI extends DDMIGroup {

	private static final String DDMI_XML = "desc/sfpdd.xml";
	private static final String MAXIM_XML = "desc/maxim_ds1856m.xml";
	
	private static final Logger LOG = Logger.getLogger(DDMI.class.getSimpleName());
	
	
	private Map<String, DataSource> _dataSources = new HashMap<String, DataSource>();
	private Map<String, DDMIElement> _elementIdCache = new HashMap<String, DDMIElement>();
	// private static final DDMI _SINGLETON;
	
	/*
	static {
		DDMILoader loader = new DDMILoader();
		
		LOG.info("Loading internal DDMI description from " + DDMI_XML );
		
		// , DDMI.class.getResource(MAXIM_XML)
		try {
			loader.load(DDMI.class.getResource(DDMI_XML));
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		_SINGLETON = loader.getDDMI();
	}
	
	public DDMI() {
	}
	*/
	
	

	
	public void prettyPrint(DDMIContext ctx) {
		StringBuilder sb = new StringBuilder();
		output(sb, ctx, 0);
		System.out.println(sb);
	}
	
	
	public void addDataSource(DataSource ds) {
		_dataSources.put(ds.getId(), ds);
	}
	
	
	/*
	
	public static final DDMI getDefault() {
		return _SINGLETON;
	}
	 */


	public DataSource getSourceById(String id) {
		return _dataSources.get(id);
	}
	

	public DDMIElement getElementById(String id)
	{
		
		if (!_elementIdCache.containsKey(id)) 
		{
			_elementIdCache.put(id, findElementForId(id));
		}
		
		return _elementIdCache.get(id);
	}
	
	


	/**
	 * Searches the entire DDMI element tree searching for element matching
	 * the provided filter. 
	 * 
	 * @param filter	The filter to use
	 * @return			A collection of matched elements
	 */
	public <T extends DDMIElement> List<T> findElements(DDMIFilter<T> filter)
	{
		List <T> list = new ArrayList<T>(); 
		
		findElements(filter, list);
		
		return list;
	}


	public boolean verifyChecksums(DDMIContext ctx) {
		boolean allOk = true;
		for (DataSource ds : _dataSources.values())
		{
			if (!ds.isValid(ctx)) continue;
			if (!ds.verifyChecksums(ctx)) {
				allOk = false;
			}
			
		}
		return allOk;
	}
	
	public void updateChecksums(DDMIContext ctx) {
		for (DataSource ds : _dataSources.values())
		{
			ds.updateChecksums(ctx);
		}
		
	}


	public Collection<DataSource> getSources() {
		return Collections.unmodifiableCollection(_dataSources.values());
	}
	


}

