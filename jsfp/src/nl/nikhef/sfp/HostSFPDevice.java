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

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import io.dvlopt.linux.i2c.I2CBuffer;
import io.dvlopt.linux.i2c.I2CBus;
import io.dvlopt.linux.i2c.I2CFlag;
import io.dvlopt.linux.i2c.I2CFlags;
import io.dvlopt.linux.i2c.I2CTransaction;
import nl.nikhef.sfp.i2c.I2CLink;

public class HostSFPDevice extends SFPDeviceBase {

  private static final Logger LOG = Logger.getLogger(HostSFPDevice.class.getSimpleName());

  private String _serial;
  private Object _monitor;
  private I2CLink _link;

  public HostSFPDevice(File dev, String serial, Object monitor) throws IOException {
    _serial = serial;
    _monitor = monitor;
    _link = new RealI2CLink(dev);
  }

  @Override
  public String getSerial() {
    return _serial;
  }

  @Override
  public int getBayCount() {
    return 1;
  }

  @Override
  public I2CLink getLink(int bay) throws IOException {
    return _link;
  }

  @Override
  public boolean isModulePresent(int bay) {
    return true;
  }

  @Override
  public boolean isTxFault(int bay) {
    return false;
  }

  @Override
  public boolean isRxLoss(int bay) {
    return false;
  }

  @Override
  public void updateModules() {
  }

  private class RealI2CLink implements I2CLink {

    private File _dev;
    private I2CBus _bus;

    public RealI2CLink(File dev) {
      _dev = dev;
    }

    @Override
    public void open() throws IOException {
      _bus = new I2CBus(_dev.toString());
    }

    @Override
    public void close() throws IOException {
      if (_bus != null) {
        _bus.close();
        _bus = null;
      }
    }

    @Override
    public void shutdown() throws IOException {
    }

    @Override
    public void i2cWrite(int addr, byte[] data) throws IOException {
      _bus.selectSlave(addr);
      I2CBuffer buffer = new I2CBuffer(data.length);
      for (int i = 0; i < data.length; i++) {
        buffer.set(i, data[i]);
      }
    }

    @Override
    public byte[] i2cRead(int addr, int len) throws IOException {
      I2CBuffer block = new I2CBuffer(len) ;
      _bus.selectSlave(addr);
      _bus.read(block, len);
      ByteBuffer bb = ByteBuffer.allocate(len);
      for (int i = 0; i < len; i++) {
        bb.put((byte)block.get(i));
      }
      return bb.array();
    }

    @Override
    public byte[] i2cWrRd(int addr, byte[] dataWr, boolean contRd, int rdLen) throws IOException {
      I2CBuffer wrbuf = new I2CBuffer(dataWr.length) ;
      I2CBuffer rdbuf = new I2CBuffer(rdLen) ;
      for (int i = 0; i < dataWr.length; i++) {
        wrbuf.set(i, dataWr[i]);
      }

      I2CTransaction trx = new I2CTransaction(2);
      trx.getMessage(0).setAddress(addr).setBuffer(wrbuf);
      I2CBuffer bufferReponse = new I2CBuffer(rdLen) ;
      trx.getMessage(1).setAddress(addr)
        .setFlags(new I2CFlags().set(I2CFlag.READ)).setBuffer(rdbuf);
      _bus.doTransaction(trx) ;

      ByteBuffer bb = ByteBuffer.allocate(rdLen);
      for (int i = 0; i < rdLen; i++) {
        bb.put((byte)rdbuf.get(i));
      }
      return bb.array();
    }

  }

  @Override
  public void shutdown() {
  }
}
