package esthesis.services.dashboard.impl.resource;

import static esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Action.REFRESHSUB;
import static esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Action.UNSUB;

import esthesis.core.common.AppConstants;
import esthesis.core.common.AppConstants.Security.Category;
import esthesis.core.common.AppConstants.Security.Operation;
import esthesis.service.audit.ccc.Audited;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.service.dashboard.resource.DashboardResource;
import esthesis.services.dashboard.impl.service.BroadcasterService;
import esthesis.services.dashboard.impl.service.DashboardService;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Component;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Subject;
import esthesis.util.kafka.notifications.outgoing.KafkaNotification;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.sse.SseEventSink;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the {@link DashboardResource} interface.
 */
@Slf4j
@RequiredArgsConstructor
public class DashboardResourceImpl implements DashboardResource {

	private final DashboardService dashboardService;
	private final BroadcasterService broadcasterService;

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	public void sub(String dashboardId, String subscriptionId, @Context SseEventSink sseEventSink) {
		broadcasterService.register(dashboardId, subscriptionId, sseEventSink);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@KafkaNotification(component = Component.DASHBOARD, subject = Subject.DASHBOARD, action = REFRESHSUB,
		idParamOrder = 0, broadcast = true)
	public Response refreshSub(String subscriptionId) {
		log.debug("Sending a Kafka notification for refreshing dashboard subscription '{}'.",
			subscriptionId);

		return Response.ok().build();
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@KafkaNotification(component = Component.DASHBOARD, subject = Subject.DASHBOARD, action = UNSUB,
		idParamOrder = 0, broadcast = true)
	public void unsub(String dashboardId) {
		log.debug("Sending a Kafka notification for unsubscribing from dashboard '{}'.", dashboardId);
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.DASHBOARD, op = Operation.READ, msg = "View dashboard")
	public Optional<DashboardEntity> findById(@PathParam("id") String id) {
		return dashboardService.findByIdOptional(id);
	}

	@Override
	public List<DashboardEntity> findAllForCurrentUser() {
		return dashboardService.findAllForCurrentUser();
	}

	@Override
	public List<DashboardEntity> findShared() {
		return dashboardService.findShared();
	}

	@Override
	@RolesAllowed(AppConstants.ROLE_USER)
	@Audited(cat = Category.DASHBOARD, op = Operation.DELETE, msg = "Delete dashboard")
	public Response delete(String id) {
		return dashboardService.deleteById(id) ? Response.ok().build()
			: Response.status(Status.INTERNAL_SERVER_ERROR).build();
	}

	@Override
	@RolesAllowed({AppConstants.ROLE_USER, AppConstants.ROLE_SYSTEM})
	@Audited(cat = Category.DASHBOARD, op = Operation.WRITE, msg = "Save dashboard")
	public DashboardEntity save(DashboardEntity dashboardEntity) {
		if (dashboardEntity.getId() == null) {
			return dashboardService.saveNew(dashboardEntity);
		} else {
			return dashboardService.saveUpdate(dashboardEntity);
		}
	}

}
