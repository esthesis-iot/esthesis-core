package esthesis.service.provisioning.impl.resource;

import esthesis.common.AppConstants.Audit.Category;
import esthesis.common.AppConstants.Audit.Operation;
import esthesis.service.audit.ccc.Audited;
import esthesis.service.audit.ccc.Audited.AuditLogType;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import esthesis.service.provisioning.form.ProvisioningPackageForm;
import esthesis.service.provisioning.impl.service.ProvisioningService;
import esthesis.service.provisioning.resource.ProvisioningResource;
import io.smallrye.mutiny.Uni;
import java.util.List;
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
  @Path("/v1/find")
  @Audited(cat = Category.PROVISIONING, op = Operation.READ, msg = "Search provisioning packages"
      , log = AuditLogType.DATA_IN)
  public Page<ProvisioningPackageEntity> find(@BeanParam Pageable pageable) {
    return provisioningService.find(pageable);
  }

  @Override
  @Audited(cat = Category.PROVISIONING, op = Operation.WRITE, msg = "Recache all provisioning "
      + "packages")
  public void recacheAll() {
    provisioningService.cacheAll();
  }

  @Override
  @Audited(cat = Category.PROVISIONING, op = Operation.READ, msg = "View provisioning package")
  public ProvisioningPackageEntity findById(ObjectId id) {
    return provisioningService.findById(id);
  }

  @Override
  @Audited(cat = Category.PROVISIONING, op = Operation.WRITE, msg = "Recache provisioning package")
  public void recache(ObjectId provisioningPackageId) {
    provisioningService.recache(provisioningPackageId);
  }

  @Override
  @Audited(cat = Category.PROVISIONING, op = Operation.WRITE, msg = "Save provisioning package")
  public ProvisioningPackageEntity save(ProvisioningPackageForm pf) {
    return provisioningService.save(pf);
  }

  @Override
  @Audited(cat = Category.PROVISIONING, op = Operation.DELETE, msg = "Delete provisioning package")
  public void delete(ObjectId provisioningPackageId) {
    provisioningService.delete(provisioningPackageId);
  }

  @Override
  @Audited(cat = Category.PROVISIONING, op = Operation.READ, msg = "Download provisioning package")
  public Uni<RestResponse<byte[]>> download(ObjectId provisioningPackageId) {
    ProvisioningPackageEntity pp = provisioningService.findById(provisioningPackageId);
    Uni<byte[]> binary = provisioningService.download(provisioningPackageId);

    return binary.onItem().transform(b -> ResponseBuilder.ok(b)
        .header(HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=\"" + pp.getFilename() + "\"")
        .header(HttpHeaders.CONTENT_LENGTH, pp.getSize())
        .build());
  }

  @Override
  public List<ProvisioningPackageEntity> findByTags(String tags) {
    return provisioningService.findByTags(tags);
  }

}
