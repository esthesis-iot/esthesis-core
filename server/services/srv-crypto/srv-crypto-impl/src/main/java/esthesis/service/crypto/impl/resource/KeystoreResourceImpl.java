package esthesis.service.crypto.impl.resource;

import com.github.slugify.Slugify;
import esthesis.common.AppConstants.Audit.Category;
import esthesis.common.AppConstants.Audit.Operation;
import esthesis.service.audit.ccc.Audited;
import esthesis.service.audit.ccc.Audited.AuditLogType;
import esthesis.service.common.paging.JSONReplyFilter;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.crypto.entity.KeystoreEntity;
import esthesis.service.crypto.impl.service.KeystoreService;
import esthesis.service.crypto.resource.KeystoreResource;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

public class KeystoreResourceImpl implements KeystoreResource {

  @Inject
  KeystoreService keystoreService;

  @GET
  @Override
  @Path("/v1/find")
  @JSONReplyFilter(filter = "content,content.id,content.name,content.description,content.createdAt")
  @Audited(cat = Category.CRYPTO, op = Operation.READ, msg = "Search keystores",
      log = AuditLogType.DATA_IN)
  public Page<KeystoreEntity> find(Pageable pageable) {
    return keystoreService.find(pageable);
  }

  @Override
  @Audited(cat = Category.CRYPTO, op = Operation.READ, msg = "View keystore")
  public KeystoreEntity findById(String id) {
    return keystoreService.findById(id);
  }

  @Override
  public KeystoreEntity save(KeystoreEntity keystoreEntity) {
    return keystoreService.save(keystoreEntity);
  }

  @Override
  public void delete(String id) {
    keystoreService.deleteById(id);
  }

  @Override
  public Response download(String id) {
    KeystoreEntity keystoreEntity = keystoreService.findById(id);

    // Try to guess the filename extension.
    String filename = Slugify.builder().underscoreSeparator(true).build()
        .slugify(keystoreEntity.getName());
    if (keystoreEntity.getType().contains("PKCS12")) {
      filename = filename + ".p12";
    } else if (keystoreEntity.getType().contains("JKS")) {
      filename = filename + ".jks";
    } else {
      filename = filename + ".keystore";
    }

    byte[] keystore = keystoreService.download(id);
    return ResponseBuilder.ok(keystore)
        .header("Content-Disposition", "attachment; filename=" + (filename).toLowerCase()).build()
        .toResponse();
  }
}
