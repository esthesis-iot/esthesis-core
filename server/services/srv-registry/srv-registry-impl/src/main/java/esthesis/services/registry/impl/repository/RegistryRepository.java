package esthesis.services.registry.impl.repository;

import esthesis.service.registry.dto.RegistryEntry;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class RegistryRepository implements
    PanacheMongoRepository<RegistryEntry> {

}
