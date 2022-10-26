package esthesis.service.application.resource;

import esthesis.service.application.dto.Application;
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
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "ApplicationResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface ApplicationResource {

  @GET
  @Path("/v1/application/find")
  Page<Application> find(@BeanParam Pageable pageable);

  @GET
  @Path("/v1/application/{id}")
  Application findById(@PathParam("id") ObjectId id);

  @DELETE
  @Path("/v1/application/{id}")
  Response delete(@PathParam("id") ObjectId id);

  @POST
  @Path("/v1/application")
  @Produces("application/json")
  Application save(@Valid Application application);
}
