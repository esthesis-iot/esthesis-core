package esthesis.device.runtime.repository;

import esthesis.device.runtime.model.Registration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class RegistrationRepository {
  private final JsonDbRepository jsonDbRepository;

  public RegistrationRepository(
    JsonDbRepository jsonDbRepository) {
    this.jsonDbRepository = jsonDbRepository;
  }

  public List<Registration> findAll() {
    return jsonDbRepository.getTemplate().findAll(Registration.class);
  }

  public Registration findById(String id) {
    return jsonDbRepository.getTemplate().findById(id, Registration.class);
  }

  public void save(Registration registration) {
    if (registration.getId() == null) {
      registration.setId(UUID.randomUUID().toString());
    }
    jsonDbRepository.getTemplate().upsert(registration);
  }
}
