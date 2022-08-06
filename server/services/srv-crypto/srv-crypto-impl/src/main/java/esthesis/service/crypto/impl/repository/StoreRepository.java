package esthesis.service.crypto.impl.repository;

import esthesis.service.crypto.dto.Store;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class StoreRepository implements PanacheMongoRepository<Store> {

}
