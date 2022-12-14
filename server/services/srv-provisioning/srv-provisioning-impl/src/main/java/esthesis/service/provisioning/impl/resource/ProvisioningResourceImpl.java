package esthesis.service.provisioning.impl.resource;

import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import esthesis.service.provisioning.form.ProvisioningPackageForm;
import esthesis.service.provisioning.impl.service.ProvisioningService;
import esthesis.service.provisioning.resource.ProvisioningResource;
import io.smallrye.mutiny.Uni;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.HttpHeaders;
import org.bson.types.ObjectId;
import org.jboss.resteasy.reactive.RestResponse;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

public class ProvisioningResourceImpl implements ProvisioningResource {

  @Inject
  ProvisioningService provisioningService;

  @GET
  @Override
  @Path("/v1/provisioning/find")
  public Page<ProvisioningPackageEntity> find(@BeanParam Pageable pageable) {
    return provisioningService.find(pageable);
  }

  @Override
  public void recacheAll() {
    provisioningService.cacheAll();
  }

  @Override
  public ProvisioningPackageEntity findById(ObjectId id) {
    return provisioningService.findById(id);
  }

  @Override
  public void recache(ObjectId provisioningPackageId) {
    provisioningService.recache(provisioningPackageId);
  }

  @Override
  public ProvisioningPackageEntity save(ProvisioningPackageForm pf) {
    return provisioningService.save(pf);
  }

  @Override
  public void delete(ObjectId provisioningPackageId) {
    provisioningService.delete(provisioningPackageId);
  }

  @Override
  public Uni<RestResponse<byte[]>> download(ObjectId provisioningPackageId) {
    ProvisioningPackageEntity pp = provisioningService.findById(provisioningPackageId);
    Uni<byte[]> binary = provisioningService.download(provisioningPackageId);

    return binary.onItem().transform(b -> ResponseBuilder.ok(b)
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + pp.getFilename() + "\"")
        .header(HttpHeaders.CONTENT_LENGTH, pp.getSize())
        .build());
  }

}
