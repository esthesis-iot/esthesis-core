package esthesis.services.settings.impl.repository;

import esthesis.service.settings.entity.DevicePageFieldEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Quarkus Panache repository for {@link DevicePageFieldEntity}.
 */
@ApplicationScoped
public class DevicePageFieldRepository implements
	PanacheMongoRepository<DevicePageFieldEntity> {

}
