package esthesis.service.dataflow.impl.resource;

import esthesis.common.AppConstants;
import esthesis.common.AppConstants.Security.Category;
import esthesis.common.AppConstants.Security.Operation;
import esthesis.service.audit.ccc.Audited;
import esthesis.service.audit.ccc.Audited.AuditLogType;
import esthesis.service.common.paging.JSONReplyFilter;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.dataflow.dto.DockerTagsDTO;
import esthesis.service.dataflow.entity.DataflowEntity;
import esthesis.service.dataflow.impl.service.DataflowService;
import esthesis.service.dataflow.resource.DataflowResource;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import java.util.List;

public class DataflowResourceImpl implements DataflowResource {

	@Inject
	DataflowService dataflowService;

	@GET
	@Override
	@Path("/v1/find")
	@RolesAllowed(AppConstants.ROLE_USER)
	@JSONReplyFilter(filter = "content,content.id,content.name,content.type,"
		+ "content.status,content.description")
	@Audited(cat = Category.DATAFLOW, op = Operation.READ, msg = "Search dataflows",
		log = AuditLogType.DATA_IN)
	public Page<DataflowEntity> find(@BeanParam Pageable pageable) {
		return dataflowService.find(pageable, true);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.DATAFLOW, op = Operation.READ, msg = "View dataflow")
	public DataflowEntity findById(String id) {
		return dataflowService.findById(id);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.DATAFLOW, op = Operation.DELETE, msg = "Delete dataflow")
	public Response delete(String dataflowId) {
		dataflowService.delete(dataflowId);
		return Response.ok().build();
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.DATAFLOW, op = Operation.WRITE, msg = "Save dataflow")
	public DataflowEntity save(DataflowEntity dataflowEntity) {
		if (dataflowEntity.getId() == null) {
			return dataflowService.saveNew(dataflowEntity);
		} else {
			return dataflowService.saveUpdate(dataflowEntity);
		}
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public DockerTagsDTO getImageTags(String dflType) {
		return dataflowService.getImageTags(dflType);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public List<String> getNamespaces() {
		return dataflowService.getNamespaces();
	}
}
