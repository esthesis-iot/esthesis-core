package esthesis.service.crypto.impl.repository;

import esthesis.service.crypto.entity.StoreEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StoreEntityRepository implements PanacheMongoRepository<StoreEntity> {

}
