package esthesis.services.tag.service;

import esthesis.common.dto.Tag;
import esthesis.common.service.BaseService;
import esthesis.common.util.validation.CVException;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TagService extends BaseService<Tag> {

  @Override
  public Tag save(Tag dto) {
    // Ensure no other tag has the same name.
    Optional<Tag> existingTag = findByColumn("name", dto.getName());
    if (existingTag.isPresent() && (dto.getId() == null || !existingTag.get()
        .getId()
        .equals(dto.getId()))) {
      new CVException<Tag>()
          .addViolation("name", "A tag with this name already exists.")
          .throwCVE();
    }

    return super.save(dto);
  }
}
