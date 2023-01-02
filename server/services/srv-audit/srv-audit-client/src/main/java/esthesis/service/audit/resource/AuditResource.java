package esthesis.service.audit.resource;

import esthesis.common.AppConstants.Audit.Category;
import esthesis.common.AppConstants.Audit.Operation;
import esthesis.service.audit.entity.AuditEntity;
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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "AuditResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface AuditResource {

  @GET
  @Path("/v1/find")
  Page<AuditEntity> find(@BeanParam Pageable pageable);

  @GET
  @Path("/v1/categories")
  Category[] getCategories();

  @GET
  @Path("/v1/operations")
  Operation[] getOperations();

  @GET
  @Path("/v1/{id}")
  AuditEntity findById(@PathParam("id") ObjectId id);

  @DELETE
  @Path("/v1/{id}")
  Response delete(@PathParam("id") ObjectId id);

  @POST
  @Path("/v1")
  @Produces("application/json")
  AuditEntity save(@Valid AuditEntity tagEntity);

  @POST
  @Path("/v1/add")
  @Produces("application/json")
  AuditEntity add(@QueryParam("name") String name);

}
