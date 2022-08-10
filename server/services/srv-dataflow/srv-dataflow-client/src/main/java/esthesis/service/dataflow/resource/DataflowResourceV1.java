package esthesis.service.dataflow.resource;

import esthesis.common.rest.Page;
import esthesis.common.rest.Pageable;
import esthesis.service.dataflow.dto.Dataflow;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
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

@Path("/api/v1/dataflow")
@RegisterRestClient(configKey = "DataflowResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface DataflowResourceV1 {

  @GET
  @Path("/find")
  Page<Dataflow> find(@BeanParam Pageable pageable);

  @GET
  @Path("/{id}")
  Dataflow findById(@PathParam("id") ObjectId id);

  @DELETE
  @Path("/{id}")
  Response delete(@PathParam("id") ObjectId id);

  @POST
  @Produces("application/json")
  Dataflow save(@Valid Dataflow dataflow);
}
