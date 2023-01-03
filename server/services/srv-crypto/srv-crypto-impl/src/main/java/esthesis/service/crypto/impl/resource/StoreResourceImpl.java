package esthesis.service.crypto.impl.resource;

import com.github.slugify.Slugify;
import esthesis.common.AppConstants.Audit.Category;
import esthesis.common.AppConstants.Audit.Operation;
import esthesis.common.exception.QExceptionWrapper;
import esthesis.service.audit.ccc.Audited;
import esthesis.service.audit.ccc.Audited.AuditLogType;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.crypto.entity.StoreEntity;
import esthesis.service.crypto.impl.service.StoreService;
import esthesis.service.crypto.resource.StoreResource;
import io.quarkus.security.Authenticated;
import java.io.IOException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.jboss.resteasy.reactive.RestResponse.ResponseBuilder;

@Authenticated
public class StoreResourceImpl implements StoreResource {

  @Inject
  JsonWebToken jwt;

  @Inject
  StoreService storeService;

  @Override
  @Audited(cat = Category.CRYPTO, op = Operation.READ, msg = "Search stores", log = AuditLogType.DATA_IN)
  public Page<StoreEntity> find(Pageable pageable) {
    return storeService.find(pageable);
  }

  @Override
  @Audited(cat = Category.CRYPTO, op = Operation.READ, msg = "View store")
  public StoreEntity findById(ObjectId id) {
    return storeService.findById(id);
  }

  @Override
  @Audited(cat = Category.CRYPTO, op = Operation.WRITE, msg = "Save store")
  public StoreEntity save(StoreEntity storeEntity) {
    return storeService.save(storeEntity);
  }

  @Override
  @Audited(cat = Category.CRYPTO, op = Operation.DELETE, msg = "Delete store")
  public void delete(ObjectId id) {
    storeService.deleteById(id);
  }

  @Override
  @Audited(cat = Category.CRYPTO, op = Operation.READ, msg = "Download store")
  public Response download(ObjectId id) {
    try {
      StoreEntity storeEntity = storeService.findById(id);
      String filename = Slugify.builder().underscoreSeparator(true).build()
          .slugify(storeEntity.getName());
      byte[] keystore = storeService.download(id);

      return ResponseBuilder.ok(keystore)
          .header("Content-Disposition",
              "attachment; filename=" + (filename + "."
                  + StoreService.KEYSTORE_TYPE).toLowerCase()).build()
          .toResponse();
    } catch (CertificateException | KeyStoreException |
             NoSuchAlgorithmException | IOException | NoSuchProviderException |
             InvalidKeySpecException e) {
      throw new QExceptionWrapper("Could not download keystore.", e);
    }
  }
}
