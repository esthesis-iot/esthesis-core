package esthesis.services.tag.impl.service;

import esthesis.common.service.BaseService;
import esthesis.common.validation.CVException;
import esthesis.service.tag.dto.Tag;
import esthesis.service.tag.messaging.TagServiceMessaging;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

@ApplicationScoped
public class TagService extends BaseService<Tag> {

  @Inject
  JsonWebToken jwt;

  @Inject
  @Channel(TagServiceMessaging.CHANNEL_DELETE)
  Emitter<Tag> tagDeletedEmitter;

  @Override
  public Tag save(Tag dto) {
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
