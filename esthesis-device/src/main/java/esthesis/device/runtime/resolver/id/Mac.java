package esthesis.device.runtime.resolver.id;

import java.io.IOException;

public class Mac implements HardwareIdResolver {

  @Override
  public String resolve(String hardwareId) throws IOException, InterruptedException {
    String deviceId = HardwareIdResolverUtil.shell(new String[]{
      "/bin/sh",
      "-c",
      "ioreg -d2 -c IOPlatformExpertDevice | awk -F\\\" '/IOPlatformUUID/{print $(NF-1)}'"
    });
    return HardwareIdResolverUtil.md5(deviceId);
  }
}
