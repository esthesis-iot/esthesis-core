package esthesis.services.infrastructure.impl.resource;

import esthesis.common.AppConstants.Security.Category;
import esthesis.common.AppConstants.Security.Operation;
import esthesis.service.audit.ccc.Audited;
import esthesis.service.audit.ccc.Audited.AuditLogType;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.infrastructure.entity.InfrastructureMqttEntity;
import esthesis.service.infrastructure.resource.InfrastructureMqttResource;
import esthesis.services.infrastructure.impl.service.InfrastructureMqttService;
import jakarta.inject.Inject;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.jwt.JsonWebToken;

public class InfrastructureMqttResourceImpl implements InfrastructureMqttResource {

	@Inject
	JsonWebToken jwt;

	@Inject
	InfrastructureMqttService infrastructureMqttService;

	@Override
	@Audited(op = Operation.READ, cat = Category.INFRASTRUCTURE, msg = "Search infrastructure / "
		+ "MQTT", log = AuditLogType.DATA_IN)
	public Page<InfrastructureMqttEntity> find(@BeanParam Pageable pageable) {
		return infrastructureMqttService.find(pageable);
	}

	@Override
	@Audited(op = Operation.WRITE, cat = Category.INFRASTRUCTURE, msg = "Save infrastructure / MQTT")
	public InfrastructureMqttEntity save(InfrastructureMqttEntity mqttEntity) {
		return infrastructureMqttService.save(mqttEntity);
	}

	@Override
	@Audited(op = Operation.READ, cat = Category.INFRASTRUCTURE, msg = "View infrastructure / MQTT")
	public InfrastructureMqttEntity findById(String id) {
		return infrastructureMqttService.findById(id);
	}

	@Override
	@Audited(op = Operation.DELETE, cat = Category.INFRASTRUCTURE,
		msg = "Delete infrastructure / MQTT")
	public Response delete(String id) {
		return infrastructureMqttService.deleteById(id) ? Response.ok().build()
			: Response.notModified().build();
	}
}
