package esthesis.service.provisioning.impl.service;

import static esthesis.service.provisioning.impl.routes.ProvisioningRoutes.PROPERTY_PROVISIONING_PACKAGE_ID;

import com.mongodb.client.model.Filters;
import esthesis.common.AppConstants.Provisioning.Type;
import esthesis.common.exception.QMismatchException;
import esthesis.service.common.BaseService;
import esthesis.service.provisioning.dto.ProvisioningPackage;
import esthesis.service.provisioning.dto.ProvisioningPackageBinary;
import esthesis.service.provisioning.form.ProvisioningPackageForm;
import esthesis.service.provisioning.impl.repository.ProvisioningBinaryRepository;
import esthesis.util.redis.EsthesisRedis;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.Produce;
import org.apache.camel.component.mongodb.MongoDbConstants;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

@Slf4j
@ApplicationScoped
public class ProvisioningService extends BaseService<ProvisioningPackage> {

  @Inject
  ProvisioningBinaryRepository provisioningBinaryRepository;

  @Produce("direct:cacheAll")
  FluentProducerTemplate cacheAllEndpoint;

  @Produce("direct:cacheOne")
  FluentProducerTemplate cacheOneEndpoint;

  @Inject
  EsthesisRedis redis;

  public ProvisioningPackage save(ProvisioningPackageForm pf) {
    // Convert the uploaded form to a ProvisioningPackage.
    ProvisioningPackage p = new ProvisioningPackage();
    p.setId(pf.getId());
    p.setName(pf.getName());
    p.setDescription(pf.getDescription());
    p.setAvailable(pf.isAvailable());
    p.setVersion((pf.getVersion()));
    p.setTags(pf.getTags());
    p.setAttributes(pf.getAttributes());
    p.setTypeSpecificConfiguration(pf.getTypeSpecificConfiguration());
    p.setType(pf.getType());
    // Only for new records.
    if (pf.getId() == null) {
      p.setCreatedOn(Instant.now());
    }
    // Only for new records of ESTHESIS type.
    if (pf.getId() == null && pf.getType() == Type.ESTHESIS) {
      p.setFilename(pf.getFile().fileName());
      p.setSize(pf.getFile().size());
      p.setContentType(pf.getFile().contentType());
    }
    p = save(p);

    // Update the binary package only for new records of ESTHESIS type.
    if (pf.getId() == null && pf.getType() == Type.ESTHESIS) {
      ProvisioningPackageBinary pb = new ProvisioningPackageBinary();
      pb.setProvisioningPackage(p.getId());
      try {
        pb.setPayload(Files.readAllBytes(pf.getFile().uploadedFile()));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      provisioningBinaryRepository.persist(pb);
    }

    // If this is a new record, try to cache it.
    if (pf.getId() == null) {
      try {
//        cacheEndpoint.createAsyncProducer().start();
      } catch (Exception e) {
        log.error("Could not start caching operation.", e);
      }
    }

    return p;
  }

//  public void searchForSpecificPackageToCache(Exchange exchange) {
//    ObjectId packageId = exchange.getIn().getBody(ObjectId.class);
//    Bson equalsClause = Filters.eq("id", packageId);
//    exchange.getIn().setHeader(MongoDbConstants.CRITERIA, equalsClause);
//  }

  public void searchForPackagesToCache(Exchange exchange) {
    Bson equalsClause = Filters.and(Filters.eq("cacheStatus", 0), Filters.eq("available", true));
    exchange.getIn().setHeader(MongoDbConstants.CRITERIA, equalsClause);
  }

  public long recache(ObjectId provisioningPackageId) {
    ProvisioningPackage pp = findById(provisioningPackageId);
    pp.setCacheStatus(0);
    save(pp);

    try {
      cacheOneEndpoint.withBody(provisioningPackageId).asyncSend();
    } catch (Exception e) {
      throw new QMismatchException("Could not start caching operation.", e);
    }

    return pp.getSize();
  }

  public void saveProvisioningPackageId(Exchange exchange) {
    ProvisioningPackage pp = exchange.getIn().getBody(ProvisioningPackage.class);
    exchange.setProperty(PROPERTY_PROVISIONING_PACKAGE_ID, pp.getId());
  }

  public void cacheInRedis(Exchange exchange) {

  }

  public void test(Exchange exchange) {
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
