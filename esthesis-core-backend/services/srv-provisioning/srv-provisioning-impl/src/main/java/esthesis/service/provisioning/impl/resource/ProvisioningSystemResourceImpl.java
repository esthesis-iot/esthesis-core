package esthesis.service.provisioning.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import esthesis.service.provisioning.impl.service.ProvisioningService;
import esthesis.service.provisioning.resource.ProvisioningSystemResource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

/**
 * Implementation of {@link ProvisioningSystemResource}.
 */
public class ProvisioningSystemResourceImpl implements ProvisioningSystemResource {

	@Inject
	ProvisioningService provisioningService;

	@Override
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	public ProvisioningPackageEntity find(String hardwareId, String version) {
		return provisioningService.semVerFind(hardwareId, version);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	public ProvisioningPackageEntity findById(String provisioningPackageId) {
		return provisioningService.findById(provisioningPackageId);
	}
}
