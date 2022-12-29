package esthesis.services.infrastructure.impl.resource;

import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.infrastructure.entity.InfrastructureMqttEntity;
import esthesis.service.infrastructure.resource.InfrastructureMqttResource;
import esthesis.services.infrastructure.impl.service.InfrastructureMqttService;
import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.jwt.JsonWebToken;

public class InfrastructureMqttResourceImpl implements InfrastructureMqttResource {

  @Inject
  JsonWebToken jwt;

  @Inject
  InfrastructureMqttService infrastructureMqttService;

  @Override
  public Page<InfrastructureMqttEntity> find(@BeanParam Pageable pageable) {
    return infrastructureMqttService.find(pageable);
  }

  @Override
  public InfrastructureMqttEntity save(InfrastructureMqttEntity mqttEntity) {
    return infrastructureMqttService.save(mqttEntity);
  }

  @Override
  public InfrastructureMqttEntity findById(ObjectId id) {
    return infrastructureMqttService.findById(id);
  }

  @Override
  public Response delete(ObjectId id) {
    return infrastructureMqttService.deleteById(id) ? Response.ok().build()
        : Response.notModified().build();
  }
}
