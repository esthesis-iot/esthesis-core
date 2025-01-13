package esthesis.service.audit.resource;

import esthesis.service.audit.entity.AuditEntity;
import esthesis.service.common.paging.Page;
import io.quarkus.oidc.client.filter.OidcClientFilter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@OidcClientFilter
@RegisterRestClient(configKey = "AuditSystemResource")
public interface AuditSystemResource {

	@GET
	@Path("/v1/system/find")
	Page<AuditEntity> find(@QueryParam("entries") int entries);

	@GET
	@Path("/v1/system/count")
	Long countAll();
	
}
