package esthesis.service.provisioning.impl.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.common.exception.QMismatchException;
import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.bson.Document;

@ApplicationScoped
public class ProvisioningRepository implements PanacheMongoRepository<ProvisioningPackageEntity> {

  @Inject
  ObjectMapper mapper;

  /**
   * A convenience method to convert a Document of type {@link ProvisioningPackageEntity} to the
   * underlying ProvisioningPackage class.
   *
   * @param doc The document to convert.
   */
  public ProvisioningPackageEntity parse(Document doc) {
    String json = doc.toJson();
    try {
      ProvisioningPackageEntity provisioningPackageEntity = mapper.readValue(json,
          ProvisioningPackageEntity.class);
      provisioningPackageEntity.setId(doc.getObjectId("_id"));
      return provisioningPackageEntity;
    } catch (JsonProcessingException e) {
      throw new QMismatchException("Could not convert ProvisioningPackage Document to "
          + "ProvisioningPackage class.", e);
    }
  }

  /**
   * Find all provisioning packages that match at least one of the given tags.
   *
   * @param tagIds The tag ids to match.
   */
  public List<ProvisioningPackageEntity> findByTagIds(List<String> tagIds) {
    return list("tags in ?1", tagIds.toArray());
  }

}
