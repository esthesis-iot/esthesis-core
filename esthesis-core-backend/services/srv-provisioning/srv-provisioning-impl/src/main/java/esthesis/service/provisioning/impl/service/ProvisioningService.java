package esthesis.service.provisioning.impl.service;

import static esthesis.core.common.AppConstants.GridFS.PROVISIONING_BUCKET_NAME;
import static esthesis.core.common.AppConstants.GridFS.PROVISIONING_METADATA_NAME;
import static esthesis.core.common.AppConstants.Security.Category.PROVISIONING;
import static esthesis.core.common.AppConstants.Security.Operation.CREATE;
import static esthesis.core.common.AppConstants.Security.Operation.DELETE;
import static esthesis.core.common.AppConstants.Security.Operation.READ;
import static esthesis.core.common.AppConstants.Security.Operation.WRITE;

import esthesis.common.exception.QMismatchException;
import esthesis.core.common.AppConstants.NamedSetting;
import esthesis.core.common.AppConstants.Provisioning.Type;
import esthesis.service.common.BaseService;
import esthesis.service.common.gridfs.GridFSDTO;
import esthesis.service.common.gridfs.GridFSService;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.common.validation.CVEBuilder;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.resource.DeviceResource;
import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import esthesis.service.provisioning.impl.repository.ProvisioningRepository;
import esthesis.service.security.annotation.ErnPermission;
import esthesis.service.settings.resource.SettingsResource;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.semver4j.Semver;

/**
 * Service for provisioning packages.
 */
@Slf4j
@ApplicationScoped
public class ProvisioningService extends BaseService<ProvisioningPackageEntity> {

	@Inject
	GridFSService gridFSService;

	@ConfigProperty(name = "quarkus.mongodb.database")
	String dbName;

	@Inject
	@RestClient
	SettingsResource settingsResource;

	@Inject
	@RestClient
	DeviceResource deviceResource;

	@Inject
	ProvisioningRepository provisioningRepository;

	/**
	 * Checks if semantic versioning is enabled in the settings.
	 *
	 * @return True if enabled, false otherwise.
	 */
	private boolean isSemverEnabled() {
		return settingsResource.findByName(NamedSetting.DEVICE_PROVISIONING_SEMVER).asBoolean();
	}

	/**
	 * Saves the provisioning package entity and the binary package.
	 *
	 * @param ppe  The provisioning package entity.
	 * @param file The uploaded file.
	 * @return The saved provisioning package entity.
	 */
	private ProvisioningPackageEntity saveHandler(ProvisioningPackageEntity ppe, FileUpload file) {
		// Custom validation for version, if semver should be followed.
		if (isSemverEnabled() && !Semver.isValid(ppe.getVersion())) {
			CVEBuilder.addAndThrow("version", "Version does not follow semantic versioning scheme. "
				+ "You can switch this option off in the settings.");
		}

		if (ppe.getId() == null) {
			ppe.setCreatedOn(Instant.now());
			if (ppe.getType() == Type.INTERNAL) {
				ppe.setFilename(file.fileName());
				ppe.setSize(file.uploadedFile().toFile().length());
				ppe.setContentType(file.contentType());
			}
		} else {
			if (ppe.getType() == Type.INTERNAL) {
				ProvisioningPackageEntity existingPpe = findById(ppe.getId().toHexString());
				ppe.setFilename(existingPpe.getFilename());
				ppe.setSize(existingPpe.getSize());
				ppe.setContentType(existingPpe.getContentType());
			}
		}
		ppe.setCreatedOn(Instant.now());
		ppe = save(ppe);

		// Update the binary package only for new records of ESTHESIS type.
		if (ppe.getType() == Type.INTERNAL && file != null) {
			try {
				gridFSService.saveBinary(GridFSDTO.builder()
					.database(dbName)
					.metadataName(PROVISIONING_METADATA_NAME)
					.metadataValue(ppe.getId().toHexString())
					.bucketName(PROVISIONING_BUCKET_NAME)
					.file(file)
					.build());
				log.debug("Uploaded file {} saved successfully.", ppe.getFilename());
			} catch (IOException e) {
				throw new QMismatchException("Could not save uploaded file.", e);
			}
		}

		return ppe;
	}

	/**
	 * Creates a new provisioning package entity.
	 *
	 * @param ppe  The provisioning package entity.
	 * @param file The uploaded file.
	 * @return The saved provisioning package entity.
	 */
	@Transactional
	@ErnPermission(category = PROVISIONING, operation = CREATE)
	public ProvisioningPackageEntity saveNew(ProvisioningPackageEntity ppe, FileUpload file) {
		return saveHandler(ppe, file);
	}

	/**
	 * Updates an existing provisioning package entity.
	 *
	 * @param ppe  The provisioning package entity.
	 * @param file The uploaded file.
	 * @return The saved provisioning package entity.
	 */
	@Transactional
	@ErnPermission(category = PROVISIONING, operation = WRITE)
	public ProvisioningPackageEntity saveUpdate(ProvisioningPackageEntity ppe, FileUpload file) {
		return saveHandler(ppe, file);
	}

	/**
	 * Deletes a provisioning package entity.
	 *
	 * @param provisioningPackageId The id of the provisioning package entity.
	 */
	@Transactional
	@ErnPermission(category = PROVISIONING, operation = DELETE)
	public void delete(String provisioningPackageId) {
		// Delete the provisioning package from the database.
		Type type = findById(provisioningPackageId).getType();
		if (type.equals(Type.INTERNAL)) {
			gridFSService.deleteBinary(GridFSDTO.builder()
				.database(dbName)
				.metadataName(PROVISIONING_METADATA_NAME)
				.metadataValue(provisioningPackageId)
				.bucketName(PROVISIONING_BUCKET_NAME)
				.build());
		}
		super.deleteById(provisioningPackageId);
	}

	/**
	 * Downloads the binary package of an internal provisioning package.
	 *
	 * @param provisioningPackageId The id of the provisioning package entity.
	 * @return The binary package.
	 */
	@ErnPermission(category = PROVISIONING, operation = READ)
	public Uni<byte[]> download(String provisioningPackageId) {
		ProvisioningPackageEntity ppe = findById(provisioningPackageId);
		if (ppe.getType() != Type.INTERNAL) {
			throw new QMismatchException("Only internal provisioning packages can be downloaded.");
		}

		// Download the binary package from the database.
		return gridFSService.downloadBinary(GridFSDTO.builder()
			.database(dbName)
			.metadataName(PROVISIONING_METADATA_NAME)
			.metadataValue(provisioningPackageId)
			.bucketName(PROVISIONING_BUCKET_NAME)
			.build());
	}

	/**
	 * Finds all provisioning packages that match at least one of the tags assigned to the device.
	 *
	 * @param tags The tags assigned to the device.
	 * @return The list of provisioning packages.
	 */
	@ErnPermission(category = PROVISIONING, operation = READ)
	public List<ProvisioningPackageEntity> findByTags(String tags) {
		List<ProvisioningPackageEntity> packages = tags.isBlank()
			? getAll()
			: findByColumnIn("tags", Arrays.asList(tags.split(",")), false);
		if (isSemverEnabled()) {
			return packages.stream()
				.sorted(Comparator.comparing(ppe -> new Semver(ppe.getVersion())))
				.toList();
		} else {
			return packages.stream()
				.sorted(Comparator.comparing(ProvisioningPackageEntity::getVersion))
				.toList();
		}
	}

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
	 * @return The candidate provisioning package.
	 */
	@ErnPermission(category = PROVISIONING, operation = READ)
	public ProvisioningPackageEntity semVerFind(String hardwareId, String version) {
		// Find the tags of the device.
		log.debug("Finding provisioning packages for hardwareId '{}'.", hardwareId);
		List<DeviceEntity> deviceEntities = deviceResource.findByHardwareIds(hardwareId, false);
		if (deviceEntities.isEmpty()) {
			throw new QMismatchException("No device found with hardware id '" + hardwareId + "'.");
		}
		DeviceEntity deviceEntity = deviceEntities.get(0);
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
		if (greaterVersions.stream().filter(p -> StringUtils.isNotBlank(p.getPrerequisiteVersion()))
			.findAny()
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

	/**
	 * Finds all provisioning packages.
	 *
	 * @param pageable     Representation of page, size, and sort search parameters.
	 * @param partialMatch Whether to do a partial match.
	 * @return The list of provisioning packages.
	 */
	@Override
	@ErnPermission(category = PROVISIONING, operation = READ)
	public Page<ProvisioningPackageEntity> find(Pageable pageable, boolean partialMatch) {
		return super.find(pageable, partialMatch);
	}

	/**
	 * Finds a provisioning package by ID.
	 *
	 * @param id The ID of the entity to find.
	 * @return The provisioning package entity.
	 */
	@Override
	@ErnPermission(category = PROVISIONING, operation = READ)
	public ProvisioningPackageEntity findById(String id) {
		return super.findById(id);
	}

}
