package esthesis.services.tag.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.tag.entity.TagEntity;
import esthesis.service.tag.resource.TagSystemResource;
import esthesis.services.tag.impl.service.TagService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import java.util.List;

/**
 * Implementation of the {@link TagSystemResource} interface.
 */
public class TagSystemResourceImpl implements TagSystemResource {

	@Inject
	TagService tagService;

	@Override
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	public List<TagEntity> getAll() {
		return tagService.getAll();
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_SYSTEM)
	public TagEntity findByName(String name) {
		return tagService.findFirstByColumn("name", name);
	}
}
