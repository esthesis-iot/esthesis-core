package esthesis.service.dataflow.resource;

import esthesis.common.rest.Page;
import esthesis.common.rest.Pageable;
import esthesis.service.dataflow.dto.DataFlowMqttClientConfig;
import esthesis.service.dataflow.dto.Dataflow;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import java.util.List;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "DataflowResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface DataflowResource {

  @GET
  @Path("/v1/dataflow/find")
  Page<Dataflow> find(@BeanParam Pageable pageable);

  @GET
  @Path("/v1/dataflow/{id}")
  Dataflow findById(@PathParam("id") ObjectId id);

  @DELETE
  @Path("/v1/dataflow/{id}")
  Response delete(@PathParam("id") ObjectId id);

  @POST
  @Path("/v1/dataflow")
  @Produces("application/json")
  Dataflow save(@Valid Dataflow dataflow);

  DataFlowMqttClientConfig matchMqttServerByTags(List<String> tags);
}
