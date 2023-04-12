package esthesis.services.security.impl.repository;

import esthesis.service.security.entity.GroupEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SecurityGroupRepository implements PanacheMongoRepository<GroupEntity> {

}
