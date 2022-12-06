package esthesis.service.provisioning.impl.service;

import esthesis.common.AppConstants.Provisioning.CacheStatus;
import esthesis.common.AppConstants.Provisioning.ConfigOptions.Ftp;
import esthesis.common.AppConstants.Provisioning.Type;
import esthesis.common.exception.QMismatchException;
import esthesis.service.common.BaseService;
import esthesis.service.provisioning.dto.ProvisioningPackage;
import esthesis.service.provisioning.dto.ProvisioningPackageBinary;
import esthesis.service.provisioning.form.ProvisioningPackageForm;
import esthesis.service.provisioning.impl.repository.ProvisioningBinaryRepository;
import esthesis.util.redis.RedisUtils;
import io.smallrye.mutiny.Uni;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.Produce;
import org.bson.types.ObjectId;

@Slf4j
@ApplicationScoped
public class ProvisioningService extends BaseService<ProvisioningPackage> {

  @Inject
  ProvisioningBinaryRepository provisioningBinaryRepository;

  @Produce("direct:cacheAll")
  FluentProducerTemplate cacheAll;

  @Produce("direct:cacheOne")
  FluentProducerTemplate cacheOneEndpoint;

  @Inject
  RedisUtils redisUtils;

  public ProvisioningPackage save(ProvisioningPackageForm pf) {
    // Convert the uploaded form to a ProvisioningPackage.
    ProvisioningPackage p;
    if (pf.getId() != null) {
      p = findById(pf.getId());
    } else {
      p = new ProvisioningPackage();
    }
    p.setName(pf.getName());
    p.setDescription(pf.getDescription());
    p.setAvailable(pf.isAvailable());
    p.setVersion((pf.getVersion()));
    p.setTags(pf.getTags());
    p.setAttributes(pf.getAttributes());
    p.setTypeSpecificConfiguration(pf.getTypeSpecificConfiguration());
    p.setSha256(pf.getSha256());
    // Only for new records.
    if (pf.getId() == null) {
      p.setType(pf.getType());
      p.setCreatedOn(Instant.now());
      p.setCacheStatus(CacheStatus.NOT_STARTED);
    }
    // For new records, for ESTHESIS type use the uploaded file's filename, for other types,
    // extract the filename from the URL.
    if (pf.getId() == null) {
      switch (p.getType()) {
        case ESTHESIS -> {
          p.setFilename(pf.getFile().fileName());
          p.setSize(pf.getFile().size());
          p.setContentType(pf.getFile().contentType());
        }
        case FTP ->
            p.setFilename(Path.of(pf.fc(Ftp.FTP_PATH).orElseThrow()).getFileName().toString());
        default ->
            throw new QMismatchException("Unsupported provisioning package type: " + p.getType());
      }
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
        cacheOneEndpoint.withBody(p.getId()).asyncSend();
      } catch (Exception e) {
        log.error("Could not start caching operation.", e);
      }
    }

    return p;
  }

  /**
   * Recaches a single Provisioning Package.
   *
   * @param provisioningPackageId
   * @return
   */
  public void recache(ObjectId provisioningPackageId) {
    ProvisioningPackage pp = findById(provisioningPackageId);
    pp.setCacheStatus(CacheStatus.NOT_STARTED);
    save(pp);

    try {
      cacheOneEndpoint.withBody(provisioningPackageId).asyncSend();
    } catch (Exception e) {
      throw new QMismatchException("Could not start caching operation.", e);
    }
  }

  public void delete(ObjectId provisioningPackageId) {
    // Delete the provisioning package from cache.
    redisUtils.deleteProvisioningPackage(provisioningPackageId);

    // Delete the provisioning package from the database.
    super.deleteById(provisioningPackageId);
  }

  public void cacheAll() {
    cacheAll.asyncSend();
  }

  //  public RestResponse<Uni<byte[]>> download(ObjectId provisioningPackageId) {
  public Uni<byte[]> download(ObjectId provisioningPackageId) {
    // Get the binary content of the provisioning package.
    return redisUtils.downloadProvisioningPackage(provisioningPackageId);
  }
}
