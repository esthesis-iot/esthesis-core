package esthesis.services.tag.impl.repository;

import esthesis.service.tag.entity.TagEntity;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Collections;
import java.util.List;

/**
 * Quarkus Panache repository for {@link TagEntity}.
 */
@ApplicationScoped
public class TagRepository implements PanacheMongoRepository<TagEntity> {

	public List<TagEntity> findByName(String name) {
		return findByName(Collections.singletonList(name));
	}

	public List<TagEntity> findByNamePartial(String name) {
		return findByNamePartial(Collections.singletonList(name));
	}

	public List<TagEntity> findByName(List<String> names) {
		return find("name in ?1", names).list();
	}

	public List<TagEntity> findByNamePartial(List<String> names) {
		return find("name like ?1", names).list();
	}

}
