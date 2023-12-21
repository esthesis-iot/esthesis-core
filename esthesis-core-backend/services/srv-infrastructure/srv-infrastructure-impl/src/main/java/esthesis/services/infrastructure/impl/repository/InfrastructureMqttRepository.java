package esthesis.services.infrastructure.impl.repository;

import esthesis.service.infrastructure.entity.InfrastructureMqttEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class InfrastructureMqttRepository implements
	PanacheMongoRepository<InfrastructureMqttEntity> {

}
