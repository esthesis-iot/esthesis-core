package esthesis.service.crypto.impl.repository;

import esthesis.service.crypto.entity.CaEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CaEntityRepository implements PanacheMongoRepository<CaEntity> {

}
