package esthesis.services.tag.impl.repository;

import esthesis.service.tag.dto.Tag;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TagRepository implements PanacheMongoRepository<Tag> {

  public List<Tag> findByName(String name) {
    return findByName(Collections.singletonList(name));
  }

  public List<Tag> findByNamePartial(String name) {
    return findByNamePartial(Collections.singletonList(name));
  }

  public List<Tag> findByName(List<String> names) {
    return find("name in ?1", names).list();
  }

  public List<Tag> findByNamePartial(List<String> names) {
    return find("name like ?1", names).list();
  }

}
