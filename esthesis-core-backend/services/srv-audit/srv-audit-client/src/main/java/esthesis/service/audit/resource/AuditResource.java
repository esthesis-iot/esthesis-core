package esthesis.service.audit.resource;

import esthesis.core.common.AppConstants.Security.Category;
import esthesis.core.common.AppConstants.Security.Operation;
import esthesis.service.audit.entity.AuditEntity;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import io.quarkus.oidc.token.propagation.common.AccessToken;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for the audit service.
 */
@AccessToken
@Path("/api")
@RegisterRestClient(configKey = "AuditResource")
public interface AuditResource {

	/**
	 * Finds audit entries.
	 *
	 * @param pageable An object containing the page number, page size, and sort order.
	 * @return A page of audit entries.
	 */
	@GET
	@Path("/v1/find")
	Page<AuditEntity> find(@BeanParam Pageable pageable);

	/**
	 * Finds the available audit categories.
	 *
	 * @return A page of audit entries.
	 */
	@GET
	@Path("/v1/categories")
	Category[] getCategories();

	/**
	 * Finds the available audit operations.
	 *
	 * @return A page of audit entries.
	 */
	@GET
	@Path("/v1/operations")
	Operation[] getOperations();

	/**
	 * Finds an audit entry by its ID.
	 *
	 * @return The audit entry.
	 */
	@GET
	@Path("/v1/{id}")
	AuditEntity findById(@PathParam("id") String id);

	/**
	 * Deletes an audit entry by its ID.
	 */
	@DELETE
	@Path("/v1/{id}")
	Response delete(@PathParam("id") String id);

	/**
	 * Saves an audit entry.
	 *
	 * @param auditEntity The audit entry to save.
	 * @return The saved audit entry.
	 */
	@POST
	@Path("/v1")
	AuditEntity save(@Valid AuditEntity auditEntity);

}
