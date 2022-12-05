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
import javax.ws.rs.core.Response;
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
  public void recacheAll() {
    provisioningService.cacheAll();
  }

  @Override
  public ProvisioningPackage findById(ObjectId id) {
    return provisioningService.findById(id);
  }

  @Override
  public void recache(ObjectId provisioningPackageId) {
    provisioningService.recache(provisioningPackageId);
  }

  @Override
  public ProvisioningPackage save(ProvisioningPackageForm pf) {
    return provisioningService.save(pf);
  }

  @Override
  public void delete(ObjectId provisioningPackageId) {
    provisioningService.delete(provisioningPackageId);
  }

  @Override
  public Response download(ObjectId provisioning) {
    return null;
  }

}
