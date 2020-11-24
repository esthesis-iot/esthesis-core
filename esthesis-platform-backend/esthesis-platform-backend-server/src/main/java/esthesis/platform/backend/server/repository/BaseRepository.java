package esthesis.platform.backend.server.repository;

import esthesis.platform.backend.server.model.BaseEntity;
import org.springframework.context.annotation.Primary;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

/**
 * A base Spring {@link Repository} to be extended by your own project-specific repository.
 */
@Repository
@Primary
public interface BaseRepository<M extends BaseEntity> extends CrudRepository<M, Long>,
    QuerydslPredicateExecutor<M> {

}
