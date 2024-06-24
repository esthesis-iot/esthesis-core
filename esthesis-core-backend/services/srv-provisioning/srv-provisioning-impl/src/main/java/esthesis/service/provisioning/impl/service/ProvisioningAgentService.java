package esthesis.service.provisioning.impl.service;

import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.resource.DeviceSystemResource;
import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import esthesis.service.provisioning.impl.repository.ProvisioningRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.semver4j.Semver;

@Slf4j
@Transactional
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

		// Find all versions higher than the current one.
		List<ProvisioningPackageEntity> greaterVersions = matchedPackages.stream()
			.filter(p -> new Semver(p.getVersion()).isGreaterThan(version))
			.sorted(Comparator.comparing(p -> new Semver(p.getVersion())))
			.toList();
		log.debug("Found '{}' greater versions '{}'.", greaterVersions.size(), greaterVersions);

		// Find the first version that respects the base version requirement.
		ProvisioningPackageEntity candidateVersion;
		if (greaterVersions.stream().filter(p -> StringUtils.isNotBlank(p.getPrerequisiteVersion())).findAny()
			.isEmpty()) {
			// If the list of greater versions do not have any prerequisite versions, return the last one.
			candidateVersion = greaterVersions.get(greaterVersions.size() - 1);
		} else {
			// If the list of greater versions have prerequisite versions, then return the lowest
			// prerequisite version.
			candidateVersion = greaterVersions.stream().filter(
				p -> p.getVersion().equals(
					greaterVersions.stream()
						.filter(x -> StringUtils.isNotEmpty(x.getPrerequisiteVersion()))
						.min(Comparator.comparing(x -> new Semver(x.getPrerequisiteVersion())))
						.orElseThrow().getPrerequisiteVersion()
				)
			).findFirst().orElseThrow();
		}

		return candidateVersion;
	}

	public ProvisioningPackageEntity findById(String provisioningPackageId) {
		return provisioningRepository.findById(new ObjectId(provisioningPackageId));
	}
}
