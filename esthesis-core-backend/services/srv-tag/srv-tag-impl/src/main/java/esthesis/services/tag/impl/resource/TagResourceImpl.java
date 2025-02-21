package esthesis.services.tag.impl.resource;

import esthesis.core.common.AppConstants;
import esthesis.core.common.AppConstants.Security.Category;
import esthesis.core.common.AppConstants.Security.Operation;
import esthesis.service.audit.ccc.Audited;
import esthesis.service.audit.ccc.Audited.AuditLogType;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.tag.entity.TagEntity;
import esthesis.service.tag.resource.TagResource;
import esthesis.services.tag.impl.service.TagService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * Implementation of the @{@link TagResource} interface.
 */
@RequiredArgsConstructor
public class TagResourceImpl implements TagResource {

	private final TagService tagService;

	@GET
	@Override
	@Path("/v1/find")
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.TAGS, op = Operation.READ, msg = "Search tags", log = AuditLogType.DATA_IN)
	public Page<TagEntity> find(@BeanParam Pageable pageable) {
		return tagService.find(pageable);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public List<TagEntity> getAll() {
		return tagService.getAll();
	}

	@Override
	public TagEntity findByName(String name) {
		List<TagEntity> tags = tagService.findByName(name);

		if (tags.isEmpty()) {
			return null;
		}

		return tagService.findByName(name).getLast();
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.TAGS, op = Operation.READ, msg = "View tag")
	public TagEntity findById(@PathParam("id") String id) {
		return tagService.findById(id);
	}

	@Override
	public List<TagEntity> findByIds(String ids) {
		return tagService.findByIds(Arrays.asList(ids.split(",")));
	}

	@Override
	@RolesAllowed({AppConstants.ROLE_USER, AppConstants.ROLE_SYSTEM})
	public List<TagEntity> findByNames(@QueryParam("names") String names) {
		return tagService.findByColumnIn("name", Arrays.asList(names.split(",")));
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.TAGS, op = Operation.DELETE, msg = "Delete tag")
	public Response delete(@PathParam("id") String id) {
		return tagService.deleteById(id) ? Response.ok().build() : Response.notModified().build();
	}

	@Override
	@RolesAllowed({AppConstants.ROLE_USER, AppConstants.ROLE_SYSTEM})
	@Audited(cat = Category.TAGS, op = Operation.WRITE, msg = "Save tag")
	public TagEntity save(@Valid TagEntity tagEntity) {
		if (tagEntity.getId() == null) {
			return tagService.saveNew(tagEntity);
		} else {
			return tagService.saveUpdate(tagEntity);
		}
	}
}
