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
package nl.nikhef.rebus.ftdi;


import java.io.IOException;

import static nl.nikhef.tools.Macro.*;

/**
 * Bus-base allows for GPIO control of non-used ports on the bus emulator.
 */
public abstract class BusBase {

	/** GPIO Port A L0, (ADBUS4) */
	public static final int GPIO_AL0 = BIT(4);
	/** GPIO Port A L1, (ADBUS5) */
	public static final int GPIO_AL1 = BIT(5);
	/** GPIO Port A L2, (ADBUS6) */
	public static final int GPIO_AL2 = BIT(6);
	/** GPIO Port A L3, (ADBUS7) */
	public static final int GPIO_AL3 = BIT(7);
	
	/** GPIO Port A H0, (ACBUS0) */
	public static final int GPIO_AH0 = BIT(8);
	/** GPIO Port A H1, (ACBUS1) */
	public static final int GPIO_AH1 = BIT(9);
	/** GPIO Port A H2, (ACBUS2) */
	public static final int GPIO_AH2 = BIT(10);
	/** GPIO Port A H3, (ACBUS3) */
	public static final int GPIO_AH3 = BIT(11);
	/** GPIO Port A H4, (ACBUS4) */
	public static final int GPIO_AH4 = BIT(12);
	/** GPIO Port A H5, (ACBUS5) */
	public static final int GPIO_AH5 = BIT(13);
	/** GPIO Port A H6, (ACBUS6) */
	public static final int GPIO_AH6 = BIT(14);
	/** GPIO Port A H7, (ACBUS7) */
	public static final int GPIO_AH7 = BIT(15);

	public static final int LO_A_MASK = 0x000000F0;
	public static final int LO_X_MASK = 0x0000000F;
	public static final int HI_A_MASK = 0x0000FF00;
	
	
	protected final MPSSE mpsse;
	
	public BusBase(MPSSE mpsse) {
		this.mpsse = mpsse;
	}
	
	protected int modeMask;
	protected int outMask;
	
	
	protected abstract void setGPIOChild(int portMask) throws IOException;
	
	protected void setBusIO(int value, int direction) throws IOException
	{
		mpsse.setDataLowByte(
			(value & LO_X_MASK) 	| (outMask & LO_A_MASK), 
			(direction & LO_X_MASK) | (modeMask & LO_A_MASK)
		);
	}

	
	private void setGPIO(int portMask) throws IOException 
	{
		if ((portMask & HI_A_MASK) != 0) {
			mpsse.setDataHighByte(0xFF & (outMask >> 8), 0xFF & (modeMask >> 8));
		}
		if ((portMask & LO_A_MASK) != 0) {
			setGPIOChild(portMask & LO_A_MASK);
		}
	}
	
	private int getGPIO(int portMask) throws IOException
	{
		int out = 0;
		if ((portMask & HI_A_MASK) != 0) {
			out = mpsse.readDataHighByte() << 8;
		}
		if ((portMask & LO_A_MASK) != 0) {
			out |= mpsse.readDataLowByte();
		}
		return out & portMask;
	}
	
	/**
	 * Configure the various ports.
	 * 
	 * Just changing one can be done by applying the apportiate mask:
	 * <code>
	 * 	setMode(GPIO_AL2, GPIO_AL2 | GPIO_AH0);	// Set AL2 as output, AH0 as input
	 *  setMode(GPIO_AH0, GPIO_AH0);			// Set AH0 as output
	 *  setMode(0, GPIO_AL2);					// Set AL2 as input
	 * </code>
	 * 
	 * @param portModes		The modes, set for output, clear for input.
	 * @param portMask		The ports to modify. If not masks, port mode will not be affected.
	 * @throws IOException 
	 */
	public void setModes(int portModes, int portMask) throws IOException
	{
		modeMask = ( portModes & ~portMask ) | ( portModes & portMask );
		setGPIO(portMask);
	}
	
	/**
	 * Set a single port mode.
	 * 
	 * @param port		The port
	 * @param output	true for output, false for input
	 * @throws IOException 
	 */
	public void setMode(int port, boolean output) throws IOException {
		setModes(output ? port : 0, port);
	}
	
	/**
	 * Sets the values on the output bus.
	 * 
	 * @param portOutputs
	 * @param portMask
	 * @throws IOException 
	 */
	public void setOutputs(int portOutputs, int portMask) throws IOException
	{
		outMask = ( outMask & ~portMask ) | ( portOutputs & portMask );
		setGPIO(portMask);
	}
	
	public void setClrPort(int port, boolean high) throws IOException
	{
		setOutputs(high ? port : 0, port);
	}
	
	public void setPort(int port) throws IOException 
	{
		setClrPort(port, true);
	}

	public void clrPort(int port) throws IOException 
	{
		setClrPort(port, false);
	}
	
	/**
	 * Query the inputs.
	 * @param mask	The inputs you wish to read.
	 * @return
	 * @throws IOException 
	 */
	public int getInputs(int mask) throws IOException 
	{
		int v = getGPIO(mask);
		return v;
	}

	/**
	 * Return the state of an input port.
	 * 
	 * <em>Slow operation, to read multiple input ports, use getInputs(int)</em> 
	 * 
	 * @param port		Port to read.
	 * @return
	 * @throws IOException 
	 */
	public boolean getPort(int port) throws IOException {
		return getGPIO(port) == port;
	}
	
}
