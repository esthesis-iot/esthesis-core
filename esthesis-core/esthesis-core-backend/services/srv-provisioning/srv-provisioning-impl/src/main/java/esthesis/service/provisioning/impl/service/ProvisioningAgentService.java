package esthesis.service.provisioning.impl.service;

import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.resource.DeviceSystemResource;
import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import esthesis.service.provisioning.impl.repository.ProvisioningRepository;
import java.util.Comparator;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.semver4j.Semver;

@Slf4j
@ApplicationScoped
public class ProvisioningAgentService {

  @Inject
  @RestClient
  DeviceSystemResource deviceSystemResource;

  @Inject
  ProvisioningRepository provisioningRepository;

  /**
   * Finds the candidate provisioning package for the given hardware id and current device firmware
   * version. Versions are checked using semver using https://github.com/vdurmont/semver4j.
   * <p>
   * The selection algorithm goes as follows:
   * <ul>
   * <li>Find all provisioning packages that match at least one of the tags assigned to the
   * device.</li>
   * <l1>Find the next greater semver than the one currently installed on the device, while
   * respecting the baseversion requirement.</l1>
   * <l1>Return the first match</l1>
   *
   * </ul>
   *
   * @param hardwareId The hardware id of the device to find a provisioning package for.
   * @param version    The version of the firmware currently installed on the device.
   * @return
   */
  public ProvisioningPackageEntity find(String hardwareId, String version) {
    // Find the tags of the device.
    log.debug("Finding provisioning packages for hardwareId '{}'.", hardwareId);
    DeviceEntity deviceEntity = deviceSystemResource.findByHardwareId(hardwareId);
    log.debug("Device found with id '{}' and tags '{}'.", deviceEntity.getId(),
        deviceEntity.getTags());

    // Find matching packages based on tags.
    List<ProvisioningPackageEntity> matchedPackages = provisioningRepository.findByTagIds(
        deviceEntity.getTags());
    log.debug("Found '{}' provisioning packages '{}'.", matchedPackages.size(), matchedPackages);

    // Find candidate version.
    List<ProvisioningPackageEntity> greaterVersions = matchedPackages.stream()
        .filter(p -> new Semver(p.getVersion()).isGreaterThan(version))
        .sorted(Comparator.comparing(p -> new Semver(p.getVersion())))
        .toList();
    log.debug("Found '{}' greater versions '{}'.", greaterVersions.size(), greaterVersions);

    String candidateVersion = version;
    for (ProvisioningPackageEntity ppe : greaterVersions) {
      if (ppe.getPrerequisiteVersion() != null
          && provisioningRepository.findById(new ObjectId(ppe.getPrerequisiteVersion()))
          .getVersion().equals(version)) {
        candidateVersion = ppe.getVersion();
      } else if (ppe.getPrerequisiteVersion() != null) {
        break;
      } else {
        candidateVersion = ppe.getVersion();
      }
    }

    log.debug("Final candidate version is '{}'.", candidateVersion);

    if (candidateVersion.equals(version)) {
      return null;
    } else {
      String finalCandidateVersion = candidateVersion;
      return greaterVersions.stream().filter(
          p -> p.getVersion().equals(finalCandidateVersion)).findFirst().orElseThrow();
    }
  }

  public ProvisioningPackageEntity findById(String provisioningPackageId) {
    return provisioningRepository.findById(new ObjectId(provisioningPackageId));
  }
}
