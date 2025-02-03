package esthesis.services.security.impl.repository;

import esthesis.service.security.entity.RoleEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Quarkus Panache repository for {@link RoleEntity}.
 */
@ApplicationScoped
public class SecurityRoleRepository implements PanacheMongoRepository<RoleEntity> {

}
