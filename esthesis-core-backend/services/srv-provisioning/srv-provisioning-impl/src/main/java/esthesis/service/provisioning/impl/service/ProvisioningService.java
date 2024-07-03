package esthesis.service.provisioning.impl.service;

import esthesis.common.AppConstants.NamedSetting;
import esthesis.common.AppConstants.Provisioning.Type;
import esthesis.common.exception.QMismatchException;
import esthesis.service.common.BaseService;
import esthesis.service.common.gridfs.GridFSDTO;
import esthesis.service.common.gridfs.GridFSService;
import esthesis.service.common.validation.CVBuilder;
import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import esthesis.service.settings.resource.SettingsResource;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import java.io.IOException;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.semver4j.Semver;

@Slf4j
@Transactional
@ApplicationScoped
public class ProvisioningService extends BaseService<ProvisioningPackageEntity> {

	private static final String GRIDFS_BUCKET_NAME = "ProvisioningPackageBucket";
	private static final String GRIDFS_METADATA_NAME = "provisioningPackageId";

	@Inject
	GridFSService gridFSService;

	@ConfigProperty(name = "quarkus.mongodb.database")
	String dbName;

	@Inject
	@RestClient
	SettingsResource settingsResource;

	private boolean isSemverEnabled() {
		return settingsResource.findByName(NamedSetting.DEVICE_PROVISIONING_SEMVER).asBoolean();
	}

	@SuppressWarnings("java:S6205")
	public ProvisioningPackageEntity save(ProvisioningPackageEntity ppe, FileUpload file) {
		// Custom validation for version, if semver should be followed.
		if (isSemverEnabled() && !Semver.isValid(ppe.getVersion())) {
			throw new ConstraintViolationException(
				Collections.singleton(new CVBuilder<>()
					.path("version")
					.message("Version does not follow semantic versioning scheme. You can switch this "
						+ "option off in the settings.")
					.build()));
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
		ppe = save(ppe);

		// Update the binary package only for new records of ESTHESIS type.
		if (ppe.getType() == Type.INTERNAL && file != null) {
			try {
				gridFSService.saveBinary(GridFSDTO.builder()
					.database(dbName)
					.metadataName(GRIDFS_METADATA_NAME)
					.metadataValue(ppe.getId().toHexString())
					.bucketName(GRIDFS_BUCKET_NAME)
					.file(file)
					.build());
				log.debug("Uploaded file {} saved successfully.", ppe.getFilename());
			} catch (IOException e) {
				throw new QMismatchException("Could not save uploaded file.", e);
			}
		}

		return ppe;
	}

	public void delete(String provisioningPackageId) {
		// Delete the provisioning package from the database.
		Type type = findById(provisioningPackageId).getType();
		if (type.equals(Type.INTERNAL)) {
			gridFSService.deleteBinary(GridFSDTO.builder()
				.database(dbName)
				.metadataName(GRIDFS_METADATA_NAME)
				.metadataValue(provisioningPackageId)
				.bucketName(GRIDFS_BUCKET_NAME)
				.build());
		}
		super.deleteById(provisioningPackageId);
	}

	public Uni<byte[]> download(String provisioningPackageId) {
		ProvisioningPackageEntity ppe = findById(provisioningPackageId);
		if (ppe.getType() != Type.INTERNAL) {
			throw new QMismatchException("Only internal provisioning packages can be downloaded.");
		}

		// Download the binary package from the database.
		return gridFSService.downloadBinary(GridFSDTO.builder()
			.database(dbName)
			.metadataName(GRIDFS_METADATA_NAME)
			.metadataValue(provisioningPackageId)
			.bucketName(GRIDFS_BUCKET_NAME)
			.build());
	}

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
}
