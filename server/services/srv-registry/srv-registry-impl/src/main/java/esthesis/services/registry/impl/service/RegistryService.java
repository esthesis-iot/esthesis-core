package esthesis.services.registry.impl.service;

import esthesis.common.service.BaseService;
import esthesis.service.registry.dto.RegistryEntry;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Slf4j
@ApplicationScoped
public class RegistryService extends BaseService<RegistryEntry> {

  @Inject
  JsonWebToken jwt;

  public RegistryEntry findByName(String name) {
    log.debug("Looking up key '{}'.", name);
    RegistryEntry registryEntry = findByColumn("name", name);
    log.debug("Found value '{}'.", registryEntry);

    return registryEntry;
  }

  public List<RegistryEntry> findByNames(String names) {
    return Arrays.stream(names.split(","))
        .map(this::findByName).collect(Collectors.toList());
  }
}
