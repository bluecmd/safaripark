package nl.nikhef.rebus.ftdi;

import com.ftdi.FTD2XXException;
import com.ftdi.FTDevice;

public class TestBinding {

	public static void main(String[] args) throws FTD2XXException {
		
		for (FTDevice dev: FTDevice.getDevices())
		{
			System.out.println(dev);
			dev.open();
			dev.write(0);
			dev.close();
		}
		
	}
	
}
