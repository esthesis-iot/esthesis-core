package esthesis.service.application.resource;

import esthesis.service.application.entity.ApplicationEntity;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
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
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "ApplicationResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface ApplicationResource {

  @GET
  @Path("/v1/find")
  Page<ApplicationEntity> find(@BeanParam Pageable pageable);

  @GET
  @Path("/v1/{id}")
  ApplicationEntity findById(@PathParam("id") String id);

  @DELETE
  @Path("/v1/{id}")
  Response delete(@PathParam("id") String id);

  @POST
  @Path("/v1")
  @Produces("application/json")
  ApplicationEntity save(@Valid ApplicationEntity applicationEntity);
}
