package esthesis.services.infrastructure.impl.resource;

import esthesis.service.infrastructure.entity.InfrastructureMqttEntity;
import esthesis.service.infrastructure.resource.InfrastructureMqttSystemResource;
import esthesis.services.infrastructure.impl.service.InfrastructureMqttService;
import jakarta.inject.Inject;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Slf4j
public class InfrastructureMqttSystemResourceImpl implements InfrastructureMqttSystemResource {

	@Inject
	JsonWebToken jwt;

	@Inject
	InfrastructureMqttService infrastructureMqttService;

	@Override
	public Optional<InfrastructureMqttEntity> matchMqttServerByTags(String tags) {
		return infrastructureMqttService.matchByTags(tags)
			.filter(InfrastructureMqttEntity::isActive);
	}

	@Override
	public Optional<InfrastructureMqttEntity> matchRandomMqttServer() {
		return infrastructureMqttService.matchRandom()
			.filter(InfrastructureMqttEntity::isActive);
	}
}
