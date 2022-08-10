package esthesis.service.dataflow.impl.repository;

import esthesis.service.dataflow.dto.Dataflow;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DataflowRepository implements PanacheMongoRepository<Dataflow> {

}
