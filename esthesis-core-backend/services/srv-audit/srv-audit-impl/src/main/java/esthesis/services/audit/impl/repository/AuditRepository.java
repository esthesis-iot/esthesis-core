package esthesis.services.audit.impl.repository;

import esthesis.service.audit.entity.AuditEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AuditRepository implements PanacheMongoRepository<AuditEntity> {

}
