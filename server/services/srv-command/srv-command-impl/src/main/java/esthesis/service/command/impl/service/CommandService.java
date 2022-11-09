package esthesis.service.command.impl.service;

import esthesis.service.device.resource.DeviceResource;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class CommandService {

  @Inject
  @RestClient
  DeviceResource deviceResource;

  /**
   * Counts the number of devices with the given hardware IDs. The matching
   * algorithm is partial.
   *
   * @param hardwareIds A comma-separated list of hardware IDs.
   */
  public Long countDevicesByHardwareIds(String hardwareIds) {
    return deviceResource.countByHardwareIds(hardwareIds, true);
  }

  /**
   * Counts the number of devices with the given tags. The matching algorithm is
   * exact.
   *
   * @param tags A comma-separated list of tag names.
   */
  public Long countDevicesByTags(String tags) {
    return deviceResource.countByTags(tags, false);
  }
}
