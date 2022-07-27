package esthesis.services.tag.impl.service;

import esthesis.common.rest.Page;
import esthesis.common.rest.Pageable;
import esthesis.common.service.BaseService;
import esthesis.common.validation.CVException;
import esthesis.service.tag.dto.Tag;
import esthesis.service.tag.messaging.TagServiceMessaging;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@Slf4j
@ApplicationScoped
public class TagService extends BaseService<Tag> {

  @Inject
  JsonWebToken jwt;

  @Inject
  @Channel(TagServiceMessaging.CHANNEL_DELETE)
  Emitter<Tag> tagDeletedEmitter;

  @Override
  public Page<Tag> find(Pageable pageable) {
    log.debug("Finding all tags with '{}'.", pageable);
    return super.find(pageable);
  }

  @Override
  public Page<Tag> find(Pageable pageable, boolean partialMatch) {
    log.debug("Finding all tags with partial match with '{}'.", pageable);
    return super.find(pageable, partialMatch);
  }

  @Override
  public Tag save(Tag dto) {
    log.debug("Saving tag '{}'.", dto);
    // Ensure no other tag has the same name.
    Tag existingTag = findByColumn("name", dto.getName());
    if (existingTag != null && (dto.getId() == null || !existingTag.getId()
        .equals(dto.getId()))) {
      new CVException<Tag>()
          .addViolation("name", "A tag with name '{}' already exists.",
              dto.getName())
          .throwCVE();
    }

    return super.save(dto);
  }

  @Override
  public void deleteById(ObjectId id) {
    log.debug("Deleting tag with id '{}'.", id);
    Tag tag = findById(id);
    super.deleteById(id);
    tagDeletedEmitter.send(tag);
  }
}
