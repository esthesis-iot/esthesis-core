package esthesis.device.runtime.resolver.id;

import java.io.IOException;

public interface HardwareIdResolver {
  String resolve(String hardwareId) throws IOException, InterruptedException;
}
