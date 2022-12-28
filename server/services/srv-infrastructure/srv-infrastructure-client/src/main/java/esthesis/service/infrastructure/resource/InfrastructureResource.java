package esthesis.service.infrastructure.resource;

import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.infrastructure.entity.InfrastructureMqttEntity;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "InfrastructureResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface InfrastructureResource {

  @GET
  @Path("/v1/infrastructure/mqtt/find")
  Page<InfrastructureMqttEntity> find(@BeanParam Pageable pageable);

  @POST
  @Path("/v1/infrastructure/mqtt")
  InfrastructureMqttEntity save(@Valid InfrastructureMqttEntity mqttEntity);

  @GET
  @Path("/v1/infrastructure/mqtt/{id}")
  InfrastructureMqttEntity findById(@PathParam("id") ObjectId id);

  @DELETE
  @Path("/v1/infrastructure/{id}")
  Response delete(@PathParam("id") ObjectId id);

}
