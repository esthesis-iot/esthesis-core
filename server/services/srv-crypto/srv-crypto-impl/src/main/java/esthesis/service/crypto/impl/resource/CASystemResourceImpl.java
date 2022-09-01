package esthesis.service.crypto.impl.resource;

import esthesis.service.crypto.impl.service.CAService;
import esthesis.service.crypto.resource.CASystemResource;
import io.quarkus.security.Authenticated;
import javax.inject.Inject;
import org.bson.types.ObjectId;

@Authenticated
public class CASystemResourceImpl implements CASystemResource {

  @Inject
  CAService caService;

  @Override
  public String getCACertificate(ObjectId caId) {
    return caService.findById(caId).getCertificate();
  }
}
