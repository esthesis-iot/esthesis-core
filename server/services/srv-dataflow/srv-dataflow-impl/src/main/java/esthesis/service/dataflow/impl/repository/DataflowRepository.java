package esthesis.service.dataflow.impl.repository;

import esthesis.common.AppConstants.DataflowType;
import esthesis.service.dataflow.dto.Dataflow;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DataflowRepository implements PanacheMongoRepository<Dataflow> {

  public List<Dataflow> findByType(DataflowType dataflowType) {
    return find("type", dataflowType).stream().toList();
  }
}
