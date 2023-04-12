package esthesis.services.security.impl.repository;

import esthesis.service.security.entity.PolicyEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SecurityPolicyRepository implements PanacheMongoRepository<PolicyEntity> {

}
