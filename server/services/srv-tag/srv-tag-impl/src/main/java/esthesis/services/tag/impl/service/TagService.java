package esthesis.services.tag.impl.service;

import esthesis.common.service.BaseService;
import esthesis.common.util.validation.CVException;
import esthesis.service.tag.dto.Tag;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TagService extends BaseService<Tag> {

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
}
