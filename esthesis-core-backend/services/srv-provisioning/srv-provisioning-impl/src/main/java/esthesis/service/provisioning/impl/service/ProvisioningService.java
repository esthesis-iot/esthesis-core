package esthesis.service.provisioning.impl.service;

import esthesis.common.AppConstants.NamedSetting;
import esthesis.common.AppConstants.Provisioning.Type;
import esthesis.common.exception.QExceptionWrapper;
import esthesis.common.exception.QMismatchException;
import esthesis.service.common.BaseService;
import esthesis.service.common.validation.CVBuilder;
import esthesis.service.provisioning.entity.ProvisioningPackageBinaryEntity;
import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import esthesis.service.provisioning.form.ProvisioningPackageForm;
import esthesis.service.settings.resource.SettingsResource;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.validation.ConstraintViolationException;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.semver4j.Semver;

@Slf4j
@Transactional
@ApplicationScoped
public class ProvisioningService extends BaseService<ProvisioningPackageEntity> {

	@Inject
	ProvisioningBinaryService provisioningBinaryService;

	@Inject
	@RestClient
	SettingsResource settingsResource;

	private boolean isSemverEnabled() {
		return settingsResource.findByName(NamedSetting.DEVICE_PROVISIONING_SEMVER).asBoolean();
	}

	@SuppressWarnings("java:S6205")
	public ProvisioningPackageEntity save(ProvisioningPackageForm pf) {

		// Custom validation for version, if semver should be followed.
		if (isSemverEnabled() && !Semver.isValid(pf.getVersion())) {
			throw new ConstraintViolationException(
				Collections.singleton(new CVBuilder<>()
					.path("version")
					.message("Version does not follow semantic versioning scheme. You can switch this "
						+ "option off in the settings.")
					.build()));
		}

		// Convert the uploaded form to a ProvisioningPackage.
		ProvisioningPackageEntity ppe =
			pf.getId() != null ? findById(pf.getId().toHexString()) : new ProvisioningPackageEntity();
		ppe.setName(pf.getName());
		ppe.setDescription(pf.getDescription());
		ppe.setAvailable(pf.isAvailable());
		ppe.setVersion((pf.getVersion()));
		ppe.setTags(pf.getTags());
		ppe.setAttributes(pf.getAttributes());
		ppe.setPrerequisiteVersion(pf.getPrerequisiteVersion());
		ppe.setSha256(pf.getSha256());
		// Only for new records.
		if (pf.getId() == null) {
			ppe.setType(pf.getType());
			ppe.setCreatedOn(Instant.now());
		}
		// For new records, for INTERNAL type use the uploaded file's filename, for other types,
		// extract the filename from the URL.
		if (pf.getId() == null) {
			switch (ppe.getType()) {
				case INTERNAL -> {
					ppe.setFilename(pf.getFile().fileName());
					ppe.setSize(pf.getFile().size());
					ppe.setContentType(pf.getFile().contentType());
				}
				case EXTERNAL -> {
					ppe.setFilename(pf.getFilename());
				}
				default ->
					throw new QMismatchException("Unsupported provisioning package type: " + ppe.getType());
			}
		}
		ppe = save(ppe);

		// Update the binary package only for new records of ESTHESIS type.
		if (pf.getId() == null && pf.getType() == Type.INTERNAL) {
			ProvisioningPackageBinaryEntity pb = new ProvisioningPackageBinaryEntity();
			pb.setProvisioningPackage(ppe.getId());
			try {
				pb.setPayload(Files.readAllBytes(pf.getFile().uploadedFile()));
			} catch (IOException e) {
				throw new QExceptionWrapper("Could not read uploaded file.", e);
			}
			provisioningBinaryService.save(pb);
		}

		return ppe;
	}

	public void delete(String provisioningPackageId) {
		// Delete the provisioning package from the database.
		Type type = findById(provisioningPackageId).getType();
		if (type.equals(Type.INTERNAL)) {
			provisioningBinaryService.deleteByColumn("provisioningPackage", provisioningPackageId);
		}
		super.deleteById(provisioningPackageId);
	}

	public Uni<byte[]> download(String provisioningPackageId) {
		ProvisioningPackageEntity ppe = findById(provisioningPackageId);
		if (ppe.getType() != Type.INTERNAL) {
			throw new QMismatchException("Only internal provisioning packages can be downloaded.");
		}
		ProvisioningPackageBinaryEntity pb = provisioningBinaryService.findByColumn("provisioningPackage",
			provisioningPackageId).get(0);
		return Uni.createFrom().item(pb.getPayload());
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
