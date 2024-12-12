package esthesis.service.dashboard.resource;

import esthesis.service.dashboard.entity.DashboardEntity;
import io.quarkus.oidc.token.propagation.AccessToken;
import jakarta.validation.Valid;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.SseEventSink;
import java.util.List;
import java.util.Optional;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@AccessToken
@Path("/api")
@RegisterRestClient(configKey = "DashboardResource")
public interface DashboardResource {

	@GET
	@Path("/v1/sub/{dashboardId}/{subscriptionId}")
	void sub(@PathParam("dashboardId") String dashboardId,
		@PathParam("subscriptionId") String subscriptionId, @Context SseEventSink sseEventSink);

	@GET
	@Path("/v1/refresh-sub/{subscriptionId}")
	Response refreshSub(@PathParam("subscriptionId") String subscriptionId);

	@DELETE
	@Path("/v1/sub/{subscriptionId}")
	void unsub(@PathParam("subscriptionId") String subscriptionId);

	@GET
	@Path("/v1/{dashboardId}")
	Optional<DashboardEntity> findById(@PathParam("dashboardId") String id);

	@GET
	@Path("/v1/all-for-current-user")
	List<DashboardEntity> findAllForCurrentUser();

	@GET
	@Path("/v1/shared")
	List<DashboardEntity> findShared();

	@DELETE
	@Path("/v1/{id}")
	Response delete(@PathParam("id") String id);

	@POST
	@Path("/v1")
	@Produces("application/json")
	DashboardEntity save(@Valid DashboardEntity dashboardEntity);
}
