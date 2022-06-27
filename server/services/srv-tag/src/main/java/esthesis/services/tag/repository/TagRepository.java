package esthesis.services.tag.repository;

import esthesis.common.dto.Tag;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TagRepository implements PanacheMongoRepository<Tag> {

}
