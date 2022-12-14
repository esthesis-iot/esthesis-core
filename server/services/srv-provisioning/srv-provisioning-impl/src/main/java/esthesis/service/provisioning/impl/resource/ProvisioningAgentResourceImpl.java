package esthesis.service.provisioning.impl.resource;

import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import esthesis.service.provisioning.impl.service.ProvisioningAgentService;
import esthesis.service.provisioning.resource.ProvisioningAgentResource;
import javax.inject.Inject;

public class ProvisioningAgentResourceImpl implements ProvisioningAgentResource {

  @Inject
  ProvisioningAgentService provisioningAgentService;

  @Override
  public ProvisioningPackageEntity find(String hardwareId) {
    return provisioningAgentService.find(hardwareId);
  }
}
