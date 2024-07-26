package esthesis.services.tag.impl.resource;

import esthesis.common.AppConstants;
import esthesis.common.AppConstants.Security.Category;
import esthesis.common.AppConstants.Security.Operation;
import esthesis.service.audit.ccc.Audited;
import esthesis.service.audit.ccc.Audited.AuditLogType;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.tag.entity.TagEntity;
import esthesis.service.tag.resource.TagResource;
import esthesis.services.tag.impl.service.TagService;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import java.util.Arrays;
import java.util.List;

public class TagResourceImpl implements TagResource {

	@Inject
	TagService tagService;

	@GET
	@Override
	@Path("/v1/find")
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.TAGS, op = Operation.READ, msg = "Search tags", log = AuditLogType.DATA_IN)
	public Page<TagEntity> find(@BeanParam Pageable pageable) {
		return tagService.find(pageable, true);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public List<TagEntity> getAll() {
		return tagService.getAll();
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.TAGS, op = Operation.READ, msg = "View tag")
	public TagEntity findById(@PathParam("id") String id) {
		return tagService.findById(id);
	}

	@Override
	@RolesAllowed({AppConstants.ROLE_USER, AppConstants.ROLE_SYSTEM})
	public TagEntity findByName(@PathParam("name") String name, boolean partialMatch) {
		return tagService.findFirstByColumn("name", name, partialMatch);
	}

	@Override
	@RolesAllowed({AppConstants.ROLE_USER, AppConstants.ROLE_SYSTEM})
	public List<TagEntity> findByNames(@QueryParam("names") String names,
		boolean partialMatch) {
		return tagService.findByColumnIn("name", Arrays.asList(names.split(",")),
			partialMatch);
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
