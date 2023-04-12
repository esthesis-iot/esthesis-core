package esthesis.service.dataflow.resource;

import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.dataflow.dto.DockerTagsDTO;
import esthesis.service.dataflow.entity.DataflowEntity;
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
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "DataflowResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface DataflowResource {

  @GET
  @Path("/v1/find")
  Page<DataflowEntity> find(@BeanParam Pageable pageable);

  @GET
  @Path("/v1/{id}")
  DataflowEntity findById(@PathParam("id") String id);

  @DELETE
  @Path("/v1/{id}")
  Response delete(@PathParam("id") String id);

  @POST
  @Path("/v1")
  @Produces("application/json")
  DataflowEntity save(@Valid DataflowEntity dataflowEntity);

  @GET
  @Path("/v1/docker-tags/{dflType}")
  DockerTagsDTO getImageTags(@PathParam("dflType") String dflType);

  @GET
  @Path("/v1/namespaces")
  List<String> getNamespaces();

}
