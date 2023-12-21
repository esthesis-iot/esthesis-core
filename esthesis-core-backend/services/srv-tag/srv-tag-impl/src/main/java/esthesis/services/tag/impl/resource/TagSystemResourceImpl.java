package esthesis.services.tag.impl.resource;

import esthesis.service.tag.entity.TagEntity;
import esthesis.service.tag.resource.TagSystemResource;
import esthesis.services.tag.impl.service.TagService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

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
