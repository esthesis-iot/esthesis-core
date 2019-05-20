package esthesis.platform.server.repository;

import esthesis.platform.server.model.Tag;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends BaseRepository<Tag> {

  Optional<Tag> findByName(String name);

  Iterable<Tag> findAllByNameIn(List<String> names);

  Iterable<Tag> findAllByIdIn(List<Long> ids);
}
