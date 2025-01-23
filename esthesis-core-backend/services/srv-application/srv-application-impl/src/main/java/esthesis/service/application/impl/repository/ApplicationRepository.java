package esthesis.service.application.impl.repository;

import esthesis.service.application.entity.ApplicationEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Quarkus Panache repository for the {@link ApplicationEntity}.
 */
@ApplicationScoped
public class ApplicationRepository implements
	PanacheMongoRepository<ApplicationEntity> {

}
