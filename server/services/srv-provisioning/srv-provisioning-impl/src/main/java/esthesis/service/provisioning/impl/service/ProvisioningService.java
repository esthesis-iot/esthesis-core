package esthesis.service.provisioning.impl.service;

import esthesis.common.AppConstants.Provisioning.CacheStatus;
import esthesis.common.AppConstants.Provisioning.ConfigOption;
import esthesis.common.AppConstants.Provisioning.Type;
import esthesis.common.exception.QExceptionWrapper;
import esthesis.common.exception.QMismatchException;
import esthesis.service.common.BaseService;
import esthesis.service.provisioning.entity.ProvisioningPackageBinaryEntity;
import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import esthesis.service.provisioning.form.ProvisioningPackageForm;
import esthesis.util.redis.RedisUtils;
import io.smallrye.mutiny.Uni;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.camel.FluentProducerTemplate;
import org.apache.camel.Produce;
import org.semver4j.Semver;

@Slf4j
@ApplicationScoped
public class ProvisioningService extends BaseService<ProvisioningPackageEntity> {

  @Inject
  ProvisioningBinaryService provisioningBinaryService;

  @Produce("direct:cacheAll")
  FluentProducerTemplate cacheAll;

  @Produce("direct:cacheOne")
  FluentProducerTemplate cacheOneEndpoint;

  @Inject
  RedisUtils redisUtils;

  @SuppressWarnings("java:S6205")
  public ProvisioningPackageEntity save(ProvisioningPackageForm pf) {
    // Convert the uploaded form to a ProvisioningPackage.
    ProvisioningPackageEntity p =
        pf.getId() != null ? findById(pf.getId().toHexString()) : new ProvisioningPackageEntity();

    p.setName(pf.getName());
    p.setDescription(pf.getDescription());
    p.setAvailable(pf.isAvailable());
    p.setVersion((pf.getVersion()));
    p.setTags(pf.getTags());
    p.setAttributes(pf.getAttributes());
    p.setPrerequisiteVersion(pf.getPrerequisiteVersion());
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
        case FTP -> p.setFilename(
            Path.of(pf.fc(ConfigOption.FTP_PATH).orElseThrow()).getFileName().toString());
        case MINIO -> p.setFilename(
            Path.of(pf.fc(ConfigOption.MINIO_OBJECT).orElseThrow()).getFileName().toString());
        case WEB -> {
          try {
            p.setFilename(
                new URL(pf.fc(ConfigOption.WEB_URL).orElseThrow()).getFile().substring(1));
          } catch (MalformedURLException e) {
            throw new QMismatchException("Could not parse Web URL.", e);
          }
        }
        default ->
            throw new QMismatchException("Unsupported provisioning package type: " + p.getType());
      }
    }
    p = save(p);

    // Update the binary package only for new records of ESTHESIS type.
    if (pf.getId() == null && pf.getType() == Type.ESTHESIS) {
      ProvisioningPackageBinaryEntity pb = new ProvisioningPackageBinaryEntity();
      pb.setProvisioningPackage(p.getId());
      try {
        pb.setPayload(Files.readAllBytes(pf.getFile().uploadedFile()));
      } catch (IOException e) {
        throw new QExceptionWrapper("Could not read uploaded file.", e);
      }
      provisioningBinaryService.save(pb);
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
  public void recache(String provisioningPackageId) {
    ProvisioningPackageEntity pp = findById(provisioningPackageId);
    pp.setCacheStatus(CacheStatus.NOT_STARTED);
    save(pp);

    try {
      cacheOneEndpoint.withBody(provisioningPackageId).asyncSend();
    } catch (Exception e) {
      throw new QMismatchException("Could not start caching operation.", e);
    }
  }

  public void delete(String provisioningPackageId) {
    // Delete the provisioning package from cache.
    redisUtils.deleteProvisioningPackage(provisioningPackageId);

    // Delete the provisioning package from the database.
    Type type = findById(provisioningPackageId).getType();
    if (type.equals(Type.ESTHESIS)) {
      provisioningBinaryService.deleteByColumn("provisioningPackage", provisioningPackageId);
    }
    super.deleteById(provisioningPackageId);
  }

  public void cacheAll() {
    cacheAll.asyncSend();
  }

  public Uni<byte[]> download(String provisioningPackageId) {
    // Get the binary content of the provisioning package.
    return redisUtils.downloadProvisioningPackage(provisioningPackageId);
  }

  public List<ProvisioningPackageEntity> findByTags(String tags) {
    if (!tags.isBlank()) {
      return
          findByColumnIn("tags", Arrays.asList(tags.split(",")), false).stream()
              .sorted(Comparator.comparing(ppe -> new Semver(ppe.getVersion())))
              .toList();
    } else {
      return getAll().stream()
          .sorted(Comparator.comparing(ppe -> new Semver(ppe.getVersion())))
          .toList();
    }
  }
}
