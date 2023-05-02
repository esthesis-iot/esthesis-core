package esthesis.services.security.impl.repository;

import esthesis.service.security.entity.RoleEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SecurityRoleRepository implements PanacheMongoRepository<RoleEntity> {

}
