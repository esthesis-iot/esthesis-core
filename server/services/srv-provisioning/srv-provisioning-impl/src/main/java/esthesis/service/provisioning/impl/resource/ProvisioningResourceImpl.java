package esthesis.service.provisioning.impl.resource;

import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.provisioning.dto.ProvisioningPackage;
import esthesis.service.provisioning.form.ProvisioningPackageForm;
import esthesis.service.provisioning.impl.service.ProvisioningService;
import esthesis.service.provisioning.resource.ProvisioningResource;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.bson.types.ObjectId;

public class ProvisioningResourceImpl implements ProvisioningResource {

  @Inject
  ProvisioningService provisioningService;

  @GET
  @Override
  @Path("/v1/provisioning/find")
  public Page<ProvisioningPackage> find(@BeanParam Pageable pageable) {
    return provisioningService.find(pageable);
  }

  @Override
  public ProvisioningPackage findById(ObjectId id) {
    return provisioningService.findById(id);
  }

  @Override
  public long recache(ObjectId provisioningPackageId) {
    return provisioningService.recache(provisioningPackageId);
  }

  @Override
  public ProvisioningPackage save(ProvisioningPackageForm pf) {
    return provisioningService.save(pf);
  }

}
