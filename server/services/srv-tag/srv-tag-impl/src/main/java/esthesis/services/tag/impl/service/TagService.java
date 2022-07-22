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
    log.info("Finding tag...");
    return super.find(pageable);
  }

  @Override
  public Page<Tag> find(Pageable pageable, boolean partialMatch) {
    log.info("Finding tag2...");
    return super.find(pageable, partialMatch);
  }

  @Override
  public Tag save(Tag dto) {
    log.info("Saving tag: {}", dto);
    // Ensure no other tag has the same name.
    Tag existingTag = findByColumn("name", dto.getName());
    if (existingTag != null && (dto.getId() == null || !existingTag.getId()
        .equals(dto.getId()))) {
      new CVException<Tag>()
          .addViolation("name", "A tag with this name already exists.")
          .throwCVE();
    }

    return super.save(dto);
  }

  @Override
  public void deleteById(ObjectId id) {
    System.out.println(jwt);
    Tag tag = findById(id);
    super.deleteById(id);
    tagDeletedEmitter.send(tag);
  }
}
