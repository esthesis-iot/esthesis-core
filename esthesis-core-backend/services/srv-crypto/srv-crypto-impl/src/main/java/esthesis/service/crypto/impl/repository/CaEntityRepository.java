package esthesis.service.crypto.impl.repository;

import esthesis.service.crypto.entity.CaEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Quarkus Panache repository for the {@link CaEntity} entity.
 */
@ApplicationScoped
public class CaEntityRepository implements PanacheMongoRepository<CaEntity> {

}
