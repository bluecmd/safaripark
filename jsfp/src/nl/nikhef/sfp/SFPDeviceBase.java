/**
 * 
 */
package nl.nikhef.sfp;

import java.io.IOException;
import java.util.Collection;

import nl.nikhef.sfp.i2c.I2CLink;
import nl.nikhef.tools.ListenerManager;

/**
 * @author vincentb
 *
 */
public abstract class SFPDeviceBase implements SFPDevice {


	private final int I2C_EEPROM_ADDR = 0x50;
	private final int I2C_EEPROM_PARTNO_OFFSET = 40;
	private final int I2C_EEPROM_PARTNO_LENGTH = 16;
	private final int I2C_EEPROM_SERIAL_OFFSET = 68;
	private final int I2C_EEPROM_SERIAL_LENGTH = 16;

	private final int I2C_EEPROM_MONTYPE_OFFSET = 92;
	private final int I2C_EEPROM_MONTYPE_DDIMPL = 1 << 6;

	
	protected final ListenerManager<SFPDeviceListener> sdlMgr =
			new ListenerManager<SFPDeviceListener>(SFPDeviceListener.class); 
	
	private String[]  _modNames;
	private Boolean[] _modDigitalDiags;
	
	/* (non-Javadoc)
	 * @see nl.nikhef.sfp.SFPDevice#getModuleSerial(int)
	 */
	@Override
	public String getModuleName(int bay) {
		if (isModulePresent(bay)) 
		{
			
			if (_modNames == null) _modNames = new String[getBayCount()];
			
			if (_modNames[bay] == null) {
			
				try {
					I2CLink i2c = getLink(bay);
					
					try {
						i2c.open();
						
						byte[] modPartNoArr = i2c.i2cWrRd(I2C_EEPROM_ADDR, 
								new byte[] { (byte)I2C_EEPROM_PARTNO_OFFSET }, true, I2C_EEPROM_PARTNO_LENGTH);
						byte[] modSerialArr = i2c.i2cWrRd(I2C_EEPROM_ADDR, 
								new byte[] { (byte)I2C_EEPROM_SERIAL_OFFSET }, true, I2C_EEPROM_SERIAL_LENGTH);
						 
						_modNames[bay]=	new String(modPartNoArr).trim() + "/" + new String(modSerialArr).trim(); 
					} finally {
						i2c.close();
					}
				} catch (IOException e) {
					_modNames[bay] = String.format("%s/%d", getSerial(), bay);
				}
			}
			return _modNames[bay];

		}
		return null;
	}


	public boolean hasDiagnostics(int bay)
	{
		if (isModulePresent(bay)) 
		{
			if (_modDigitalDiags == null) _modDigitalDiags = new Boolean[getBayCount()];
			
			if (_modDigitalDiags[bay] == null) {
				
				try {
					I2CLink i2c = getLink(bay);
					
					try {
						i2c.open();
						
						byte monType = i2c.i2cWrRd(I2C_EEPROM_ADDR, 
								new byte[] { (byte)I2C_EEPROM_MONTYPE_OFFSET }, true, 1)[0];
						 
						_modDigitalDiags[bay]= Boolean.valueOf((monType & I2C_EEPROM_MONTYPE_DDIMPL ) != 0);
					} finally {
						i2c.close();
					}
				} catch (IOException e) {
					_modDigitalDiags[bay] = Boolean.FALSE;
				}
			}
			return _modDigitalDiags[bay];
			
		}
		return false;
	}
	
	protected void clearCachedSerial(int bay) {
		_modNames[bay] = null;
	}
	
	
	@Override
	public void addDeviceListener(SFPDeviceListener sdl) {
		sdlMgr.addListener(sdl);
	}

	@Override
	public Collection<SFPDeviceListener> getDeviceListeners() {
		return sdlMgr.getListeners();
	}

	@Override
	public void removeDeviceListener(SFPDeviceListener sdl) {
		sdlMgr.removeListener(sdl);
	}
	
	
	private String getKey(SFPDevice sfpDevice) {
		return sfpDevice.getSerial();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other == null) return false;
		if (!(other instanceof SFPDevice)) return false;
		
		String otherKey = getKey(SFPDevice.class.cast(other)); 
			
		return getKey(this).equals(otherKey);
	}
	
	@Override
	public int hashCode() {
		return getKey(this).hashCode();
	}
	

	@Override
	public int compareTo(SFPDevice o) {
		return getSerial().compareTo(o.getSerial());
	}

	public void updateModules()
	{
	}
	
	public void refreshInfo(int bay)
	{
		if (_modNames != null) _modNames[bay] = null;
		sdlMgr.getProxy().sfpModuleStateChanged(this, bay);
	}
	
}
