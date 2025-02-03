package esthesis.services.security.impl.repository;

import esthesis.service.security.entity.PolicyEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Quarkus Panache repository for {@link PolicyEntity}.
 */
@ApplicationScoped
public class SecurityPolicyRepository implements PanacheMongoRepository<PolicyEntity> {

}
