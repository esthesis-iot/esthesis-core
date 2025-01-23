package esthesis.service.audit.resource;

import esthesis.service.audit.entity.AuditEntity;
import esthesis.service.common.paging.Page;
import io.quarkus.oidc.client.filter.OidcClientFilter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for the audit system service, when used by SYSTEM OIDC clients.
 */
@Path("/api")
@OidcClientFilter
@RegisterRestClient(configKey = "AuditSystemResource")
public interface AuditSystemResource {

	/**
	 * Finds audit entries.
	 *
	 * @param entries The number of entries to return.
	 * @return A page of audit entries.
	 */
	@GET
	@Path("/v1/system/find")
	Page<AuditEntity> find(@QueryParam("entries") int entries);

	/**
	 * Counts all audit entries.
	 *
	 * @return The number of audit entries.
	 */
	@GET
	@Path("/v1/system/count")
	Long countAll();

}
