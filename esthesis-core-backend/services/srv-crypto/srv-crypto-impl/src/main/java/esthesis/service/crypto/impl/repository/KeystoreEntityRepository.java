package esthesis.service.crypto.impl.repository;

import esthesis.service.crypto.entity.KeystoreEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Quarkus Panache repository for the {@link KeystoreEntity} entity.
 */
@ApplicationScoped
public class KeystoreEntityRepository implements PanacheMongoRepository<KeystoreEntity> {

}
