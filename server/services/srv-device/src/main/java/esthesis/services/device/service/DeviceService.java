package esthesis.services.device.service;

import esthesis.dto.Device;
import esthesis.services.device.repository.DeviceRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;

@ApplicationScoped
public class DeviceService {

  @Inject
  JsonWebToken jwt;

  @Inject
  DeviceRepository deviceRepository;

  public List<Device> findAll() {
    return deviceRepository.listAll();
  }

  public Device createDevice() {
    Device device = new Device();
    device.setState("OK");
    device.setLastSeen(Instant.now());
    List<String> tags = new ArrayList<>();
    tags.add("tag1");
    tags.add("tag2");
    device.setTags(tags);
    deviceRepository.persistOrUpdate(device);

    return device;
  }

  public String test() {
    return jwt.toString();
  }
}
