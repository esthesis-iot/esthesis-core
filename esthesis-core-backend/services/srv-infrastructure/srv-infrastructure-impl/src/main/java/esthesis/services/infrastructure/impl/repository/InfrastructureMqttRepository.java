package esthesis.services.infrastructure.impl.repository;

import esthesis.service.infrastructure.entity.InfrastructureMqttEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Qarkus Panache Repository for {@link InfrastructureMqttEntity}.
 */
@ApplicationScoped
public class InfrastructureMqttRepository implements
	PanacheMongoRepository<InfrastructureMqttEntity> {

}
