package esthesis.services.security.impl.repository;

import esthesis.service.security.entity.UserEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Quarkus Panache repository for {@link UserEntity}.
 */
@ApplicationScoped
public class SecurityUserRepository implements PanacheMongoRepository<UserEntity> {

}
