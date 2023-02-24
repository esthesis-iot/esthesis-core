package esthesis.service.crypto.resource;

import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.crypto.entity.KeystoreEntity;
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
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/keystore")
@RegisterRestClient(configKey = "KeystoreResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface KeystoreResource {

  @GET
  @Path("/v1/find")
  Page<KeystoreEntity> find(@BeanParam Pageable pageable);

  @GET
  @Path("/v1/{id}")
  KeystoreEntity findById(@PathParam("id") String id);

  @POST
  @Path("/v1")
  KeystoreEntity save(@Valid KeystoreEntity keystoreEntity);

  @DELETE
  @Path("/v1/{id}")
  void delete(@PathParam("id") String id);

  @GET
  @Path("/v1/{id}/download")
  @Produces(MediaType.APPLICATION_OCTET_STREAM)
  Response download(@PathParam("id") String id);

}
