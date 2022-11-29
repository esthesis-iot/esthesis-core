package esthesis.services.tag.impl.service;

import esthesis.service.common.BaseService;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.common.validation.CVException;
import esthesis.service.tag.dto.Tag;
import esthesis.service.tag.messaging.TagServiceMessaging;
import esthesis.services.tag.impl.repository.TagRepository;
import io.opentelemetry.context.Context;
import io.smallrye.reactive.messaging.TracingMetadata;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;

@Slf4j
@ApplicationScoped
public class TagService extends BaseService<Tag> {

  @Inject
  JsonWebToken jwt;

  @Inject
  @Channel(TagServiceMessaging.TOPIC_TAG_DELETE)
  Emitter<Tag> tagDeletedEmitter;

  @Inject
  TagRepository tagRepository;

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
    Tag existingTag = findFirstByColumn("name", dto.getName());
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
  public boolean deleteById(ObjectId id) {
    log.debug("Deleting tag with id '{}'.", id);
    Tag tag = findById(id);
    if (tag != null) {
      boolean isDeleted = super.deleteById(id);
      log.debug("Emitting tag deleted message for tag '{}'.", tag);
      tagDeletedEmitter.send(Message.of(tag).addMetadata(
          TracingMetadata.withCurrent(Context.current())));
      return isDeleted;
    } else {
      log.warn("Tag with id '{}' not found to be deleted.", id);
      return false;
    }
  }

  public List<Tag> findByName(String name, boolean partialMatch) {
    return findByName(Collections.singletonList(name), partialMatch);
  }

  public List<Tag> findByName(List<String> names, boolean partialMatch) {
    if (partialMatch) {
      return tagRepository.findByNamePartial(names);
    } else {
      return tagRepository.findByName(names);
    }
  }
}
