package esthesis.service.crypto.impl.repository;

import esthesis.service.crypto.entity.KeystoreEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class KeystoreEntityRepository implements PanacheMongoRepository<KeystoreEntity> {

}
