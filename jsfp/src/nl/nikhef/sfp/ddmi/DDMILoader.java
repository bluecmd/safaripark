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

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;

import org.xml.sax.SAXException;

import nl.nikhef.tools.Conversion;
import nl.nikhef.tools.Converter;
import nl.nikhef.tools.xml.FXML;
import nl.nikhef.tools.xml.SimpleObjectProxy;
import nl.nikhef.tools.xml.SimpleProxyFactory;
import nl.nikhef.tools.xml.XOL;
import nl.nikhef.tools.xml.XOLProxy;
import nl.nikhef.tools.xml.XOLProxyBase;
import nl.nikhef.tools.xml.XOLProxyFactory;

public class DDMILoader {

	private static final String SFPDD_XML = "desc/sfpdd.xml";
	private static final String SFPDD_XSD = "desc/sfpdd.xsd";
	
	private XOL _xol = new XOL();
	private FXML _fxml;
	private DDMI _ddmi;
	
	private static class DDMIMapMetaProxy extends XOLProxyBase<Void> {

		private DDMIValue _value;
		private int		  _key;
		
		@Override
		public boolean setAttribute(String name, String obj) {
			if (name.equals("key")) {
				_key = Integer.parseInt(obj);
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void setParent(XOLProxy<?> obj) {
			_value = DDMIValue.class.cast(obj.getInstance());
		}

		@Override
		public boolean setContent(String data) {
			DDMIMeta.LOOKUP.of(_value).put(_key, data);
			return true;
		}

		@Override
		public String getTypeName() {
			return "DDMI Lookup Meta";
		}

	};

	private static class DDMIConstMetaProxy extends XOLProxyBase<Void> {

		private DDMIValue _value;
		
		@Override
		public boolean setAttribute(String name, String obj) {
			return true;
		}

		@Override
		public void setParent(XOLProxy<?> obj) {
			_value = DDMIValue.class.cast(obj.getInstance());
		}

		@Override
		public boolean setContent(String data) {
			_value.setMeta(DDMIMeta.CONST, data);
			return true;
		}

		@Override
		public String getTypeName() {
			return "DDMI Const Meta";
		}

	};	
	
	private static class DDMIBitFieldMetaProxy extends XOLProxyBase<Void> {

		private DDMIValue _value;
		private int		  _bit;
		
		@Override
		public boolean setAttribute(String name, String obj) {
			if (name.equals("bit")) {
				_bit = Integer.parseInt(obj);
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void setParent(XOLProxy<?> obj) {
			_value = DDMIValue.class.cast(obj.getInstance());
		}

		@Override
		public boolean setContent(String data) {
			DDMIMeta.BITFIELD.of(_value).setBit(data, _bit, false);
			return true;
		}

		@Override
		public String getTypeName() {
			return "DDMI Bitfield Meta";
		}

		
	};
	
	private static class DDMIConversionMetaProxy extends XOLProxyBase<Void> {

		private DDMIValue _value;
		private float	  _scale = 0.0f;
		private float	  _offset = 0.0f;
		
		@Override
		public boolean setAttribute(String name, String obj) {
			if (name.equals("scale")) {
				_scale = Float.parseFloat(obj);
				return true;
			} else if (name.equals("offset")) {
				_offset = Float.parseFloat(obj);
				return true;
			} else {
				return false;
			}
		}

		@Override
		public void setParent(XOLProxy<?> obj) {
			_value = DDMIValue.class.cast(obj.getInstance());
		}


		@Override
		public void complete() {
			DDMIMeta.CONV.of(_value).setScaling(_scale, _offset);
		}
		
		@Override
		public String getTypeName() {
			return "DDMI Conversion Meta";
		}

		
	};

	
	
	private static class DDMIMapMetaFactory implements XOLProxyFactory<Void> {

		private DDMIMeta<?> _meta;
		
		public DDMIMapMetaFactory(DDMIMeta<?> meta) {
			_meta = meta;
		}
		
		@Override
		public XOLProxy<Void> newProxy() {
			if (_meta == DDMIMeta.LOOKUP) {
				return new DDMIMapMetaProxy();
			} 
			if (_meta == DDMIMeta.BITFIELD) {
				return new DDMIBitFieldMetaProxy();
			} 
			
			if (_meta == DDMIMeta.CONV) {
				return new DDMIConversionMetaProxy();
			} 
			
			if (_meta == DDMIMeta.CONST) {
				return new DDMIConstMetaProxy();
			} 

			
			return null;
		}
		
	}
	
	private static class DDMIValueFactory implements XOLProxyFactory<DDMIValue> {

		private DDMIValue.DDMIType _typ;
		
		public DDMIValueFactory(DDMIValue.DDMIType typ)
		{
			_typ = typ;
		
		}
		@Override
		public XOLProxy<? extends DDMIValue> newProxy() {
			
			
			return new SimpleObjectProxy<DDMIValue>(new DDMIValue(_typ));
		}
		
	}
	
	public static class SourceProxy extends XOLProxyBase<DataSource>
	{

		private int _i2cAddress = -1;
		private DataSource _parent = null;
		private int _page = -1;
		private int _pageSelect = -1;
		private String _id;
		private String _condition;
		private int _start = 0;
		private int _end = 255;
		
		
		private List<ChecksumProxy> _checksumProxy = new ArrayList<ChecksumProxy>();
		private List<CacheProxy>    _cacheProxy = new ArrayList<CacheProxy>();
		
		@Override
		public boolean setAttribute(String name, String val) 
		{
			if (name.equals("i2c-addr")) 
			{
				_i2cAddress = XOL.parseInt(val);
				return true;
			} 
			if (name.equals("id")) {
				_id = val;
				return true;
			}
			if (name.equals("page-select")) {
				_pageSelect = XOL.parseInt(val);
				return true;
			}
			if (name.equals("page")) {
				_page =  XOL.parseInt(val);
				return true;
			}

			if (name.equals("start")) {
				_start =  XOL.parseInt(val);
				return true;
			}
			
			if (name.equals("end")) {
				_end =  XOL.parseInt(val);
				return true;
			}
			
			if (name.equals("parent-id")) {
				
				XOLProxy<?> proxy = getXol().getProxyById(val);
				if (proxy == null) throw new RuntimeException(String.format("DataSource with ID '%s' not found", val));
				_parent = SourceProxy.class.cast(proxy).getInstance();				
				return true;
			}
			if (name.equals("valid-if")) {
				_condition = val;
				return true;
			}
			
			return false;
		}
		
		@Override
		public void addChild(XOLProxy<?> obj)
		{
			if (obj instanceof ChecksumProxy) {
				_checksumProxy.add(ChecksumProxy.class.cast(obj));
			} else if (obj instanceof CacheProxy) {
				_cacheProxy.add(CacheProxy.class.cast(obj));
			} else {
				throw new RuntimeException("DataSource can't accept " + obj.getTypeName());
			}
		}
		
		
		@Override
		public DataSource getInstance() {
			
			DataSource ds = null;
			
			if (_i2cAddress >= 0) {
				ds = new I2CDataSource(_i2cAddress, _start, _end); 
			} else if (_page != -1) {
				ds = new PageDataSource(_parent, _page, _start, _end);
			}
			
			if (ds == null) throw new RuntimeException("DataSource element not correctly attributted");
			
			for (ChecksumProxy cp : _checksumProxy) {
				ds.addChecksum(cp.offset, cp.start, cp.end);				
			}
			
			for (CacheProxy cp : _cacheProxy) {
				ds.addCache(cp.start, cp.end);				
			}

			
			if (_condition != null) {
				ds.setValidIf(_condition);
			}
			
			if (_id != null) ds.setId(_id);
			
			if (_pageSelect >= 0) ds.setPageSelect(_pageSelect);
			
			return ds;
		}

		@Override
		public String getTypeName() {
			return "DataSource";
		}

	}
	
	public static class ChecksumProxy extends XOLProxyBase<Void> {

		public int start = -1;
		public int end = -1;
		public int offset = -1;
		
		
		@Override
		public boolean setAttribute(String name, String val) 
		{
			if (name.equals("start")) {
				start = XOL.parseInt(val);
				return true;
			}
			if (name.equals("offset")) {
				offset = XOL.parseInt(val);
				return true;
			}
			if (name.equals("end")) {
				end = XOL.parseInt(val);
				return true;
			}
				
			
			return false;
		}


		@Override
		public String getTypeName() {
			return "Checksum";
		}
	}
	public static class CacheProxy extends XOLProxyBase<Void> {

		public int start = -1;
		public int end = -1;
		
		
		@Override
		public boolean setAttribute(String name, String val) 
		{
			if (name.equals("start")) {
				start = XOL.parseInt(val);
				return true;
			}
			if (name.equals("end")) {
				end = XOL.parseInt(val);
				return true;
			}
			
			return false;
		}


		@Override
		public String getTypeName() {
			return "Cache";
		}
		

		
	}
	
	
	
	public DDMILoader() throws IOException, XMLStreamException, SAXException {
		this(DDMILoader.class.getResource(SFPDD_XML), 
			 DDMILoader.class.getResource(SFPDD_XSD));
	}
	
	public DDMILoader(URL base, URL xsd) throws IOException, XMLStreamException, SAXException
	{
		_xol.setMapping("source", new SimpleProxyFactory<DataSource>(SourceProxy.class));
		_xol.setMapping("checksum", new SimpleProxyFactory<Void>(ChecksumProxy.class));
		_xol.setMapping("cache", new SimpleProxyFactory<Void>(CacheProxy.class));
		_xol.setMapping("group", DDMIGroup.class);
		_xol.setMapping("ddmi", DDMI.class);
		_xol.setMapping("int", new DDMIValueFactory(DDMIValue.DDMIType.INT_TYPE));
		_xol.setMapping("uint", new DDMIValueFactory(DDMIValue.DDMIType.UINT_TYPE));
		_xol.setMapping("bitmap", new DDMIValueFactory(DDMIValue.DDMIType.BITMAP_TYPE));
		_xol.setMapping("password", new DDMIValueFactory(DDMIValue.DDMIType.PASSWORD_TYPE));
		_xol.setMapping("text", new DDMIValueFactory(DDMIValue.DDMIType.TEXT_TYPE));
		_xol.setMapping("float", new DDMIValueFactory(DDMIValue.DDMIType.DECIMAL_TYPE_FLOAT));
		_xol.setMapping("sfix", new DDMIValueFactory(DDMIValue.DDMIType.DECIMAL_TYPE_SFIXED));
		_xol.setMapping("ufix", new DDMIValueFactory(DDMIValue.DDMIType.DECIMAL_TYPE_UFIXED));
		
		
		Converter.add(new Conversion(String.class, ViewLevel.class) {

			@Override
			public Object convert(Object from) {
				String s = String.class.cast(from);
				return ViewLevel.valueOf(s.toUpperCase());
			}
		
		});
		
		_xol.setMapping("map", new DDMIMapMetaFactory(DDMIMeta.LOOKUP));
		_xol.setMapping("bool", new DDMIMapMetaFactory(DDMIMeta.BITFIELD));
		_xol.setMapping("scale", new DDMIMapMetaFactory(DDMIMeta.CONV));
		_xol.setMapping("const", new DDMIMapMetaFactory(DDMIMeta.CONST));
		
		
		_fxml = new FXML(base, xsd);
		
	}
	
	
	public void loadOverlays(URL ... overlays) throws IOException
	{
		for (URL overlay : overlays) {
			try {
				_fxml.loadOverlay(overlay, DDMILoader.class.getResource(SFPDD_XSD));
			} catch (Exception e)
			{
				throw new IOException("Failed to load overlay " + overlay, e);
			}
		}
		_ddmi = null;
	}


	public DDMI getDDMI() {
		if (_ddmi == null) {
			_ddmi = DDMI.class.cast(_xol.loadObject(_fxml).get(0));
		}
		return _ddmi;
	}


	
}

