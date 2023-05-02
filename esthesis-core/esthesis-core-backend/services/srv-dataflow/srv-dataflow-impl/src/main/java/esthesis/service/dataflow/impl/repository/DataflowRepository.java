package esthesis.service.dataflow.impl.repository;

import esthesis.service.dataflow.entity.DataflowEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class DataflowRepository implements PanacheMongoRepository<DataflowEntity> {

	public List<DataflowEntity> findByType(String dataflowType) {
		return find("type", dataflowType).stream().toList();
	}
}
