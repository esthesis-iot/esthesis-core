package esthesis.services.tag.impl.repository;

import esthesis.service.tag.dto.Tag;
import io.quarkus.mongodb.panache.PanacheMongoRepository;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TagRepository implements PanacheMongoRepository<Tag> {

}
