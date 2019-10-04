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
package nl.nikhef.safaripark.res;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import nl.nikhef.safaripark.SaFariPark;

public class Resources {

	public static final Icon getIcon(String name) {
		URL resource = Resources.class.getResource(name + ".png");
		try {
			BufferedImage bi = ImageIO.read(resource);
			return new ImageIcon(bi);
		} catch (IOException e) {
		}
		return null;		
	}

	public static Image getImage(String name) {
		URL imgURL = Resources.class.getResource(name + ".png");
		try {
			return ImageIO.read(imgURL);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
}
