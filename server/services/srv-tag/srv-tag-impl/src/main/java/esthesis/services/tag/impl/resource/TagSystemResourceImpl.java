package esthesis.services.tag.impl.resource;

import esthesis.service.tag.entity.TagEntity;
import esthesis.service.tag.resource.TagSystemResource;
import esthesis.services.tag.impl.service.TagService;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class TagSystemResourceImpl implements TagSystemResource {

  @Inject
  TagService tagService;

  @Override
  public List<TagEntity> getAll() {
    return tagService.getAll();
  }

  @Override
  public TagEntity findByName(String name) {
    return tagService.findFirstByColumn("name", name, false);
  }

}
