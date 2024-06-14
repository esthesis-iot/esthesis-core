package esthesis.service.provisioning.impl.resource;

import esthesis.common.AppConstants;
import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import esthesis.service.provisioning.impl.service.ProvisioningAgentService;
import esthesis.service.provisioning.resource.ProvisioningAgentResource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;

public class ProvisioningAgentResourceImpl implements ProvisioningAgentResource {

	@Inject
	ProvisioningAgentService provisioningAgentService;

	@Override
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	public ProvisioningPackageEntity find(String hardwareId, String version) {
		return provisioningAgentService.find(hardwareId, version);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	public ProvisioningPackageEntity findById(String provisioningPackageId) {
		return provisioningAgentService.findById(provisioningPackageId);
	}
}
