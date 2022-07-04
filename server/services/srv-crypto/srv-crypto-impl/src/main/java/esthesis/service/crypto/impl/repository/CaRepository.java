package esthesis.service.crypto.impl.repository;

import esthesis.service.crypto.dto.Ca;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CaRepository implements PanacheMongoRepository<Ca> {

}
