package esthesis.service.audit.resource;

import esthesis.common.AppConstants.Security.Category;
import esthesis.common.AppConstants.Security.Operation;
import esthesis.service.audit.entity.AuditEntity;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
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
	AuditEntity findById(@PathParam("id") String id);

	@DELETE
	@Path("/v1/{id}")
	Response delete(@PathParam("id") String id);

	@POST
	@Path("/v1")
	@Produces("application/json")
	AuditEntity save(@Valid AuditEntity auditEntity);

}
