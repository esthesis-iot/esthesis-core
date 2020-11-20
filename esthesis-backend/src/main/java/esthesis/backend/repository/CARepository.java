package esthesis.backend.repository;

import esthesis.backend.model.Ca;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CARepository extends BaseRepository<Ca> {

  Ca findByCn(String cn);
  List<Ca> getAllByPrivateKeyIsNotNullOrderByCn();
}
