package esthesis.services.infrastructure.impl.resource;

import esthesis.service.infrastructure.entity.InfrastructureMqttEntity;
import esthesis.service.infrastructure.resource.InfrastructureSystemResource;
import esthesis.services.infrastructure.impl.service.InfrastructureMqttService;
import java.util.Optional;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Slf4j
public class InfrastructureSystemResourceImpl implements InfrastructureSystemResource {

  @Inject
  JsonWebToken jwt;

  @Inject
  InfrastructureMqttService infrastructureMqttService;

  @Override
  public Optional<InfrastructureMqttEntity> matchMqttServerByTags(String tags) {
    return infrastructureMqttService.matchByTags(tags)
        .filter(InfrastructureMqttEntity::isActive);
  }
}
