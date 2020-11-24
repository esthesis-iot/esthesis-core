package esthesis.device.runtime.resolver.id;

import java.io.IOException;

public class Rpi3bp implements HardwareIdResolver {

  @Override
  public String resolve(String hardwareId) throws IOException, InterruptedException {
    String deviceId = HardwareIdResolverUtil.shell(new String[]{
      "/bin/cat",
      "/sys/firmware/devicetree/base/serial-number"
    });

    String macId = HardwareIdResolverUtil.shell(new String[]{
      "/bin/cat",
      "/sys/class/net/eth0/address"
    });

    return HardwareIdResolverUtil.hashGenerator(deviceId + macId);
  }
}
