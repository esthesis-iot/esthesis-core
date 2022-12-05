package esthesis.service.provisioning.impl.service;

import static esthesis.service.provisioning.impl.routes.ProvisioningRoutes.PROPERTY_EXCEPTION_MESSAGE;
import static esthesis.service.provisioning.impl.routes.ProvisioningRoutes.PROPERTY_PROVISIONING_PACKAGE_HASH;
import static esthesis.service.provisioning.impl.routes.ProvisioningRoutes.PROPERTY_PROVISIONING_PACKAGE_ID;
import static esthesis.service.provisioning.impl.routes.ProvisioningRoutes.PROPERTY_PROVISIONING_PACKAGE_SIZE;
import static esthesis.service.provisioning.impl.routes.ProvisioningRoutes.PROPERTY_PROVISIONING_PACKAGE_TYPE;

import com.mongodb.client.model.Filters;
import esthesis.common.AppConstants.Provisioning.CacheStatus;
import esthesis.common.exception.QMismatchException;
import esthesis.service.provisioning.dto.ProvisioningPackage;
import esthesis.service.provisioning.impl.repository.ProvisioningRepository;
import esthesis.util.redis.RedisUtils;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.Exchange;
import org.apache.camel.component.mongodb.MongoDbConstants;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.BsonDocument;
import org.bson.BsonInt64;
import org.bson.BsonNull;
import org.bson.BsonString;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

@Slf4j
@ApplicationScoped
public class ProvisioningRoutingService {

  @Inject
  ProvisioningRepository provisioningRepository;

  @Inject
  RedisUtils redisUtils;

  public void searchAllActive(Exchange exchange) {
    Bson equalsClause = Filters.eq("available", true);
    exchange.getIn().setHeader(MongoDbConstants.CRITERIA, equalsClause);
  }

  /**
   * Parses an incoming {@link ProvisioningPackage} and extracts as exchange properties: - The ID -
   * The type of the package (FTP, Web, etc.). - The hash.
   *
   * @param exchange The Camel Exchange.
   */
  public void extractProvisioningPackageInfo(Exchange exchange) {
    // Parse the incoming package.
    ProvisioningPackage pp = provisioningRepository.parse(
        exchange.getIn().getBody(Document.class));

    // Extract the id.
    exchange.setProperty(PROPERTY_PROVISIONING_PACKAGE_ID, pp.getId());

    // Extract the hash.
    if (StringUtils.isNotBlank(pp.getSha256())) {
      exchange.setProperty(PROPERTY_PROVISIONING_PACKAGE_HASH, pp.getSha256());
    }

    // Extract the type.
    exchange.setProperty(PROPERTY_PROVISIONING_PACKAGE_TYPE, pp.getType());
  }

  /**
   * Caches the binary payload of this package to Redis.
   *
   * @param exchange The Camel Exchange.
   */
  public void cacheInRedis(Exchange exchange) {
    String packageId = exchange.getProperty(PROPERTY_PROVISIONING_PACKAGE_ID, String.class);
    byte[] packageBinary = exchange.getIn().getBody(byte[].class);
    redisUtils.cacheProvisioningPackage(packageId, packageBinary);
    exchange.setProperty(PROPERTY_PROVISIONING_PACKAGE_SIZE, packageBinary.length);
  }

  /**
   * Prepares the database update operation when the caching operation has failed.
   *
   * @param exchange The Camel Exchange.
   */
  public void setFailureConditions(Exchange exchange) {
    Bson equalsClause = Filters.eq("_id",
        exchange.getProperty(PROPERTY_PROVISIONING_PACKAGE_ID, ObjectId.class));
    exchange.getIn().setHeader(MongoDbConstants.CRITERIA, equalsClause);

    BsonDocument updateObj = new BsonDocument().append("$set",
        new BsonDocument()
            .append("cacheStatus", new BsonString(CacheStatus.FAILED.toString()))
            .append("log",
                new BsonString(exchange.getProperty(PROPERTY_EXCEPTION_MESSAGE, String.class))));
    exchange.getIn().setBody(updateObj);
  }

  /**
   * Prepares the database update operation when the caching operation has succeeded.
   *
   * @param exchange The Camel Exchange.
   */
  public void setSuccessConditions(Exchange exchange) {
    Bson equalsClause = Filters.eq("_id",
        exchange.getProperty(PROPERTY_PROVISIONING_PACKAGE_ID, ObjectId.class));
    exchange.getIn().setHeader(MongoDbConstants.CRITERIA, equalsClause);

    BsonDocument updateObj = new BsonDocument().append("$set",
        new BsonDocument()
            .append("cacheStatus", new BsonString(CacheStatus.COMPLETED.toString()))
            .append("log", new BsonNull())
            .append("size", new BsonInt64(
                exchange.getProperty(PROPERTY_PROVISIONING_PACKAGE_SIZE, Long.class))));
    exchange.getIn().setBody(updateObj);
  }

  public void setCacheStatusToInProgress(Exchange exchange) {
    Bson equalsClause = Filters.eq("_id",
        exchange.getProperty(PROPERTY_PROVISIONING_PACKAGE_ID, ObjectId.class));
    exchange.getIn().setHeader(MongoDbConstants.CRITERIA, equalsClause);

    BsonDocument updateObj = new BsonDocument().append("$set",
        new BsonDocument().append("cacheStatus",
            new BsonString(CacheStatus.IN_PROGRESS.toString())));
    exchange.getIn().setBody(updateObj);
  }

  public void checkHash(Exchange exchange) {
    String packageId = exchange.getProperty(PROPERTY_PROVISIONING_PACKAGE_ID, String.class);
    String providedHash = exchange.getProperty(PROPERTY_PROVISIONING_PACKAGE_HASH, String.class);
    if (!StringUtils.isBlank(providedHash)) {
      byte[] packageBinary = exchange.getIn().getBody(byte[].class);
      String calculatedHash = DigestUtils.sha256Hex(packageBinary);
      if (!providedHash.equals(calculatedHash)) {
        throw new QMismatchException("Hash mismatch for provisioning package '{}', provided '{}',"
            + " calculated '{}'.", packageId, providedHash, calculatedHash);
      } else {
        log.debug("Hash '{}' for provisioning package '{}' matches.", providedHash, packageId);
      }
    } else {
      log.debug("No hash provided for provisioning package '{}'.", packageId);
    }
  }
}
