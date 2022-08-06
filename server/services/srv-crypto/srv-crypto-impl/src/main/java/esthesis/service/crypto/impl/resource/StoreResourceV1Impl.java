package esthesis.service.crypto.impl.resource;

import com.github.slugify.Slugify;
import esthesis.common.rest.Page;
import esthesis.common.rest.Pageable;
import esthesis.service.crypto.dto.Store;
import esthesis.service.crypto.impl.service.StoreService;
import esthesis.service.crypto.resource.StoreResourceV1;
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
public class StoreResourceV1Impl implements StoreResourceV1 {

  @Inject
  JsonWebToken jwt;

  @Inject
  StoreService storeService;

  @Override
  public Page<Store> find(Pageable pageable) {
    return storeService.find(pageable);
  }

  @Override
  public Store findById(ObjectId id) {
    return storeService.findById(id);
  }

  @Override
  public Store save(Store store) {
    return storeService.save(store);
  }

  @Override
  public void delete(ObjectId id) {
    storeService.deleteById(id);
  }

  @Override
  public Response download(ObjectId id) {
    try {
      Store store = storeService.findById(id);
      String filename = Slugify.builder().underscoreSeparator(true).build()
          .slugify(store.getName());
      byte[] keystore = storeService.download(id);

      return ResponseBuilder.ok(keystore)
          .header("Content-Disposition",
              "attachment; filename=" + (filename + "."
                  + StoreService.KEYSTORE_TYPE).toLowerCase()).build()
          .toResponse();
    } catch (CertificateException | KeyStoreException |
             NoSuchAlgorithmException | IOException | NoSuchProviderException |
             InvalidKeySpecException e) {
      throw new RuntimeException("Could not download keystore.", e);
    }
  }
}
