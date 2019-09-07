package esthesis.device.runtime.resolver.id;

public interface HardwareIdResolver {
  String resolve(String hardwareId) throws Exception;
}
