package esthesis.service.provisioning.impl.service;

import esthesis.service.common.BaseService;
import esthesis.service.provisioning.dto.ProvisioningPackage;
import esthesis.service.provisioning.dto.ProvisioningPackageBinary;
import esthesis.service.provisioning.form.ProvisioningPackageForm;
import esthesis.service.provisioning.impl.repository.ProvisioningBinaryRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Instant;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class ProvisioningService extends BaseService<ProvisioningPackage> {

  @Inject
  ProvisioningBinaryRepository provisioningBinaryRepository;

  public ProvisioningPackage save(ProvisioningPackageForm pf) {
    // Convert the uploaded form to a ProvisioningPackage and a ProvisioningPackageBinary.
    ProvisioningPackage p = new ProvisioningPackage();
    p.setId(pf.getId());
    p.setName(pf.getName());
    p.setDescription(pf.getDescription());
    p.setAvailable(pf.isAvailable());
    p.setVersion((pf.getVersion()));
    p.setTags(pf.getTags());
    p.setAttributes(pf.getAttributes());
    p.setTypeSpecificConfiguration(pf.getTypeSpecificConfiguration());
    // Only for new records.
    if (pf.getId() == null) {
      p.setType(pf.getType());
      p.setFilename(pf.getFile().fileName());
      p.setSize(pf.getFile().size());
      p.setContentType(pf.getFile().contentType());
      p.setCreatedOn(Instant.now());
    }
    p = save(p);

    // Update the binary package, only if this is a new record.
    if (pf.getId() == null) {
      ProvisioningPackageBinary pb = new ProvisioningPackageBinary();
      pb.setProvisioningPackage(p.getId());
      try {
        pb.setPayload(Files.readAllBytes(pf.getFile().uploadedFile()));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      provisioningBinaryRepository.persist(pb);
    }

    return p;
  }

}
