package esthesis.service.dataflow.impl.repository;

import esthesis.service.dataflow.entity.DataflowEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DataflowRepository implements PanacheMongoRepository<DataflowEntity> {

  public List<DataflowEntity> findByType(String dataflowType) {
    return find("type", dataflowType).stream().toList();
  }
}
