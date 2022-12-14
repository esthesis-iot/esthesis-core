package esthesis.service.provisioning.impl.service;

import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.resource.DeviceSystemResource;
import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import esthesis.service.provisioning.impl.repository.ProvisioningRepository;
import java.util.List;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class ProvisioningAgentService {

  @Inject
  @RestClient
  DeviceSystemResource deviceSystemResource;

  @Inject
  ProvisioningRepository provisioningRepository;

  /**
   * Finds the candidate provisioning package for the given hardware id.
   * <p>
   * The selection algorithm goes as follows:
   * <ul>
   * <li>Find all provisioning packages that match at least one of the tags assigned to the
   * device.</li>
   * <l1>Sort all matches by reverse version number and reverse created day</l1>
   * <l1>Return the first match</l1>
   *
   * </ul>
   *
   * @param hardwareId The hardware id of the device to find a provisioning package for.
   * @return
   */
  public ProvisioningPackageEntity find(String hardwareId) {
    log.debug("Finding provisioning packages for hardwareId '{}'.", hardwareId);
    DeviceEntity deviceEntity = deviceSystemResource.findByHardwareId(hardwareId);
    log.debug("Device found with id '{}' and tags '{}'.", deviceEntity.getId(),
        deviceEntity.getTags());
    List<ProvisioningPackageEntity> matchedPackages = provisioningRepository.findByTagIds(
        deviceEntity.getTags());
    log.debug("Found '{}' provisioning packages '{}'.", matchedPackages.size(), matchedPackages);

    // Sort by version and created date.
    Optional<ProvisioningPackageEntity> matchedPackage = matchedPackages.stream()
        .collect(Collectors.groupingBy(ProvisioningPackageEntity::getVersion, TreeMap::new,
            Collectors.toList()))
        .lastEntry().getValue().stream()
        .sorted((p1, p2) -> p2.getCreatedOn().compareTo(p1.getCreatedOn()))
        .findFirst();

    if (matchedPackage.isPresent()) {
      log.debug("Candidate provisioning package '{}'.", matchedPackage.get());
      return matchedPackage.get();
    } else {
      log.debug("Could not find a candidate provisioning package.");
      return null;
    }
  }

}
