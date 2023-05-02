package esthesis.service.provisioning.impl.resource;

import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import esthesis.service.provisioning.impl.service.ProvisioningAgentService;
import esthesis.service.provisioning.resource.ProvisioningAgentResource;
import jakarta.inject.Inject;

public class ProvisioningAgentResourceImpl implements ProvisioningAgentResource {

	@Inject
	ProvisioningAgentService provisioningAgentService;

	@Override
	public ProvisioningPackageEntity find(String hardwareId, String version) {
		return provisioningAgentService.find(hardwareId, version);
	}

	@Override
	public ProvisioningPackageEntity findById(String provisioningPackageId) {
		return provisioningAgentService.findById(provisioningPackageId);
	}
}
