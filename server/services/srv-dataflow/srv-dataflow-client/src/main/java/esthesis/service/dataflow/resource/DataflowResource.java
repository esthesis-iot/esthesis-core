package esthesis.service.dataflow.resource;

import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.dataflow.dto.Dataflow;
import esthesis.service.dataflow.dto.DockerTags;
import esthesis.service.dataflow.dto.MatchedMqttServer;
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

  MatchedMqttServer matchMqttServerByTags(List<String> tags);

  @GET
  @Path("/v1/dataflow/docker-tags/{dflType}")
  DockerTags getImageTags(@PathParam("dflType") String dflType);

  @GET
  @Path("/v1/dataflow/namespaces")
  List<String> getNamespaces();

}
