package esthesis.service.dataflow.impl.resource;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.core.common.AppConstants;
import esthesis.core.common.AppConstants.Security.Category;
import esthesis.core.common.AppConstants.Security.Operation;
import esthesis.service.audit.ccc.Audited;
import esthesis.service.audit.ccc.Audited.AuditLogType;
import esthesis.service.common.paging.JSONReplyFilter;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.common.validation.CVEBuilder;
import esthesis.service.dataflow.dto.FormlySelectOption;
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

/**
 * Implementation of the {@link DataflowResource} interface.
 */
public class DataflowResourceImpl implements DataflowResource {

	@Inject
	DataflowService dataflowService;

	@Inject
	ObjectMapper objectMapper;

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
		DataflowEntity retVal = dataflowEntity;

		if (dataflowEntity.getId() == null) {
			// Check if the name is available.
			String namespace = objectMapper.valueToTree(dataflowEntity.getConfig())
				.path("kubernetes").path("namespace").asText();
			if (!dataflowService.isDeploymentNameAvailable(dataflowEntity.getName(), namespace)) {
				CVEBuilder.addAndThrow("name", "Name is already in use.");
			} else {
				retVal = dataflowService.saveNew(dataflowEntity);
			}
		} else {
			retVal = dataflowService.saveUpdate(dataflowEntity);
		}

		return retVal;
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public List<FormlySelectOption> getNamespaces() {
		return dataflowService.getNamespaces();
	}
}
