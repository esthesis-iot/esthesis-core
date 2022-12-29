package esthesis.service.crypto.resource;

import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.crypto.entity.StoreEntity;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import javax.validation.Valid;
import javax.ws.rs.BeanParam;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/store")
@RegisterRestClient(configKey = "StoreResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface StoreResource {

  @GET
  @Path("/v1/find")
  Page<StoreEntity> find(@BeanParam Pageable pageable);

  @GET
  @Path("/v1/{id}")
  StoreEntity findById(@PathParam("id") ObjectId id);

  @POST
  @Path("/v1")
  StoreEntity save(@Valid StoreEntity storeEntity);

  @DELETE
  @Path("/v1/{id}")
  void delete(@PathParam("id") ObjectId id);

  @GET
  @Path("/v1/{id}/download")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  Response download(@PathParam("id") ObjectId id);

}
