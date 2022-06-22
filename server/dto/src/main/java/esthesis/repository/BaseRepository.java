package esthesis.repository;

import esthesis.dto.BaseDTO;
import io.quarkus.mongodb.panache.PanacheMongoRepository;

public interface BaseRepository<D extends BaseDTO> extends
    PanacheMongoRepository<D> {

}
