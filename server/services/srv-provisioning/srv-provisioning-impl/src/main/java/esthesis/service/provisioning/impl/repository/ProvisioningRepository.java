package esthesis.service.provisioning.impl.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.common.exception.QMismatchException;
import esthesis.service.provisioning.dto.ProvisioningPackage;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.bson.Document;

@ApplicationScoped
public class ProvisioningRepository implements PanacheMongoRepository<ProvisioningPackage> {

  @Inject
  ObjectMapper mapper;

  /**
   * A convenience method to convert a Document of type {@link ProvisioningPackage} to the
   * underlying ProvisioningPackage class.
   *
   * @param doc The document to convert.
   */
  public ProvisioningPackage parse(Document doc) {
    String json = doc.toBsonDocument().toJson();

    try {
      return mapper.readValue(json, ProvisioningPackage.class);
    } catch (JsonProcessingException e) {
      throw new QMismatchException("Could not convert ProvisioningPackage Document to "
          + "ProvisioningPackage class.", e);
    }
  }
}
