/* *****************************************************************************
 * SaFariPark SFP+ editor and support libraries
 * Copyright (C) 2020 Christian Svensson
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
package nl.nikhef.sfp;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

public class HostSFPProvider extends SFPProviderBase
{

  private static final Logger LOG = Logger.getLogger(HostSFPProvider.class.getSimpleName());

  @Override
  public String getName() {
    return "HostSFP";
  }

  public SFPDevice getDeviceBySerial(String serial) {
    for (SFPDevice dev : getDevices()) {
      if (dev.getSerial().equals(serial)) return dev;
    }
    return null;
  }

  private void considerDevice(Path devpath, String name) throws IOException {
    SFPDevice dev = getDeviceBySerial(name);
    if (dev == null) {
      dev = new HostSFPDevice(devpath.toFile(), name, this);
    }
    updAdd(dev);
  }

  public void scanForNewDevices() {
    try {
      final PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**/i2c-*/name");
      EnumSet<FileVisitOption> opts = EnumSet.of(FileVisitOption.FOLLOW_LINKS);
      Path devdir = FileSystems.getDefault().getPath("/dev");
      Files.walkFileTree(Paths.get("/sys/class/i2c-dev/"), opts, 2, new SimpleFileVisitor<Path>() {
        @Override
        public FileVisitResult visitFile(Path path,
            BasicFileAttributes attrs) throws IOException {
          if (matcher.matches(path)) {
            Scanner scanner = new Scanner(path.toFile());
            String mod = scanner.nextLine();
            scanner.close();
            if (mod.startsWith("fejkon")) {
              considerDevice(devdir.resolve(path.getParent().getFileName()), mod);
            }
          }
          return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFileFailed(Path file, IOException exc)
          throws IOException {
          return FileVisitResult.CONTINUE;
        }
      });
    } catch (IOException e) {
      e.printStackTrace();
    }
    updProcess();
  }

}
