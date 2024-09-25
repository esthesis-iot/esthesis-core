package esthesis.services.infrastructure.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.infrastructure.entity.InfrastructureMqttEntity;
import esthesis.service.infrastructure.resource.InfrastructureMqttSystemResource;
import esthesis.services.infrastructure.impl.service.InfrastructureMqttService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class InfrastructureMqttSystemResourceImpl implements InfrastructureMqttSystemResource {

	@Inject
	InfrastructureMqttService infrastructureMqttService;

	@Override
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	public Optional<InfrastructureMqttEntity> matchMqttServerByTags(String tags) {
		return infrastructureMqttService.matchByTags(tags)
			.filter(InfrastructureMqttEntity::isActive);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	public Optional<InfrastructureMqttEntity> matchRandomMqttServer() {
		return infrastructureMqttService.matchRandom()
			.filter(InfrastructureMqttEntity::isActive);
	}
}
