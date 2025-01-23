package esthesis.services.security.impl.repository;

import esthesis.service.security.entity.GroupEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Quarkus Panache repository for {@link GroupEntity}.
 */
@ApplicationScoped
public class SecurityGroupRepository implements PanacheMongoRepository<GroupEntity> {

}
