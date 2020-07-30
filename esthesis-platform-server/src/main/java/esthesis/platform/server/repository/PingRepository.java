package esthesis.platform.server.repository;

import esthesis.platform.server.model.Ping;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PingRepository extends CrudRepository<Ping, String> {

}
