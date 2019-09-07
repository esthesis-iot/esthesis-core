package esthesis.device.runtime.repository;

import esthesis.device.runtime.model.Provisioning;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ProvisioningRepository {
  private final JsonDbRepository jsonDbRepository;

  public ProvisioningRepository(
    JsonDbRepository jsonDbRepository) {
    this.jsonDbRepository = jsonDbRepository;
  }

  public long countAllByIsInitialProvisioning(boolean isInitial) {
    String query = String.format("/.[isInitialProvisioning=%s]", isInitial);
    return jsonDbRepository.getTemplate().find(query, Provisioning.class).size();
  }

  public Provisioning findByPackageId(long packageId) {
    String query = String.format("/.[packageId=%s]", packageId);
    return jsonDbRepository.getTemplate().findOne(query, Provisioning.class);
  }

  public Provisioning findById(String id) {
    return jsonDbRepository.getTemplate().findById(id, Provisioning.class);
  }

  public void save(Provisioning provisioning) {
    if (findById(provisioning.getId()) == null) {
      provisioning.setId(UUID.randomUUID().toString());
      jsonDbRepository.getTemplate().insert(provisioning);
    } else {
      jsonDbRepository.getTemplate().save(provisioning, Provisioning.class);
    }
  }
}
