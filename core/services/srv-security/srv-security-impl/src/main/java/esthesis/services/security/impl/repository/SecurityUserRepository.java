package esthesis.services.security.impl.repository;

import esthesis.service.security.entity.UserEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SecurityUserRepository implements PanacheMongoRepository<UserEntity> {

}
