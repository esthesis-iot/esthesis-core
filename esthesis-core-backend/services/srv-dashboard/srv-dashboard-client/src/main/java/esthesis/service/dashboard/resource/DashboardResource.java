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

/**
 * REST client for the dashboard service.
 */
@AccessToken
@Path("/api")
@RegisterRestClient(configKey = "DashboardResource")
public interface DashboardResource {

	/**
	 * Subscribes to a dashboard, create an SSE event sink.
	 *
	 * @param dashboardId    The dashboard id.
	 * @param subscriptionId The subscription id.
	 * @param sseEventSink   The event sink.
	 */
	@GET
	@Path("/v1/sub/{dashboardId}/{subscriptionId}")
	void sub(@PathParam("dashboardId") String dashboardId,
		@PathParam("subscriptionId") String subscriptionId, @Context SseEventSink sseEventSink);

	/**
	 * Refreshes the subscription to a dashboard. This method should be called in regular intervals to
	 * keep the underlying dashboard update job alive (the default interva should be 1 minute).
	 * <p>
	 * Since it is not known which instance of the dashboard component received this request and which
	 * instance of the dashboard component is handling the broadcaster for the user, this method
	 * triggers a refresh subscription Kafka message. All dashboard service-instances will receive
	 * this message and the one handling the broadcaster instance for the user will refresh it.
	 *
	 * @param subscriptionId The subscription ID to refresh.
	 */
	@GET
	@Path("/v1/refresh-sub/{subscriptionId}")
	Response refreshSub(@PathParam("subscriptionId") String subscriptionId);

	/**
	 * Unsubscribes from a dashboard, terminating the underlying update job and closing the SSE sink.
	 * <p>
	 * Since it is not known which instance of the dashboard component received this request and which
	 * instance of the dashboard component is handling the broadcaster for the user, this method
	 * triggers an unsubscription Kafka message. All dashboard service-instances will receive this
	 * message and the one handling  the broadcaster instance for the user will remove it.
	 *
	 * @param subscriptionId The subscription ID to terminate.
	 */
	@DELETE
	@Path("/v1/sub/{subscriptionId}")
	void unsub(@PathParam("subscriptionId") String subscriptionId);

	/**
	 * Finds a dashboard by its ID.
	 *
	 * @param id The dashboard ID.
	 * @return The dashboard entity.
	 */
	@GET
	@Path("/v1/{dashboardId}")
	Optional<DashboardEntity> findById(@PathParam("dashboardId") String id);

	/**
	 * Finds all dashboards for the current user.
	 *
	 * @return The list of dashboards.
	 */
	@GET
	@Path("/v1/all-for-current-user")
	List<DashboardEntity> findAllForCurrentUser();

	/**
	 * Finds all shared dashboards.
	 *
	 * @return The list of shared dashboards.
	 */
	@GET
	@Path("/v1/shared")
	List<DashboardEntity> findShared();

	/**
	 * Deletes a dashboard by its ID.
	 *
	 * @param id The dashboard ID.
	 * @return The response.
	 */
	@DELETE
	@Path("/v1/{id}")
	Response delete(@PathParam("id") String id);

	/**
	 * Saves a dashboard.
	 *
	 * @param dashboardEntity The dashboard entity to save.
	 * @return The saved dashboard entity.
	 */
	@POST
	@Path("/v1")
	@Produces("application/json")
	DashboardEntity save(@Valid DashboardEntity dashboardEntity);
}
