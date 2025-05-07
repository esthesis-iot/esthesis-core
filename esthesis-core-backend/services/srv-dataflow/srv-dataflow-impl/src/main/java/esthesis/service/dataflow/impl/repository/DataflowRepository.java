package esthesis.service.dataflow.impl.repository;

import esthesis.service.dataflow.entity.DataflowEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Quarkus Panache repository for {@link DataflowEntity}.
 */
@ApplicationScoped
public class DataflowRepository implements PanacheMongoRepository<DataflowEntity> {
}
