package esthesis.services.tag.impl.service;

import esthesis.service.common.BaseService;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.common.validation.CVExceptionContainer;
import esthesis.service.tag.entity.TagEntity;
import esthesis.service.tag.messaging.TagServiceMessaging;
import esthesis.services.tag.impl.repository.TagRepository;
import io.opentelemetry.context.Context;
import io.smallrye.reactive.messaging.TracingMetadata;
import java.util.Collections;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Message;

@Slf4j
@ApplicationScoped
public class TagService extends BaseService<TagEntity> {

  @Inject
  JsonWebToken jwt;

  @Inject
  @Channel(TagServiceMessaging.TOPIC_TAG_DELETE)
  Emitter<TagEntity> tagDeletedEmitter;

  @Inject
  TagRepository tagRepository;

  @Override
  public Page<TagEntity> find(Pageable pageable) {
    log.debug("Finding all tags with '{}'.", pageable);
    return super.find(pageable);
  }

  @Override
  public Page<TagEntity> find(Pageable pageable, boolean partialMatch) {
    log.debug("Finding all tags with partial match with '{}'.", pageable);
    return super.find(pageable, partialMatch);
  }

  @Override
  public TagEntity save(TagEntity tagEntity) {
    log.debug("Saving tag '{}'.", tagEntity);
    // Ensure no other tag has the same name.
    TagEntity existingTagEntity = findFirstByColumn("name", tagEntity.getName());
    if (existingTagEntity != null && (tagEntity.getId() == null || !existingTagEntity.getId()
        .equals(tagEntity.getId()))) {
      new CVExceptionContainer<TagEntity>()
          .addViolation("name", "A tag with name '{}' already exists.",
              tagEntity.getName())
          .throwCVE();
    }

    return super.save(tagEntity);
  }

  @Override
  public boolean deleteById(String id) {
    log.debug("Deleting tag with id '{}'.", id);
    TagEntity tagEntity = findById(id);
    if (tagEntity != null) {
      boolean isDeleted = super.deleteById(id);
      log.debug("Emitting tag deleted message for tag '{}'.", tagEntity);
      tagDeletedEmitter.send(Message.of(tagEntity).addMetadata(
          TracingMetadata.withCurrent(Context.current())));
      return isDeleted;
    } else {
      log.warn("Tag with id '{}' not found to be deleted.", id);
      return false;
    }
  }

  public List<TagEntity> findByName(String name, boolean partialMatch) {
    return findByName(Collections.singletonList(name), partialMatch);
  }

  public List<TagEntity> findByName(List<String> names, boolean partialMatch) {
    if (partialMatch) {
      return tagRepository.findByNamePartial(names);
    } else {
      return tagRepository.findByName(names);
    }
  }
}
