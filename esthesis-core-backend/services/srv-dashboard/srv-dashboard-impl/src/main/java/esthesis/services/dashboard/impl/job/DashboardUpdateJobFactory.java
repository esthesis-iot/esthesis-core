package esthesis.services.dashboard.impl.job;

import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.services.dashboard.impl.job.helper.AboutUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.AuditUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.CampaignsUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.DeviceMapUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.DevicesLastSeenUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.DevicesLatestUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.DevicesStatusUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.NotesUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.SecurityStatsUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.SensorIconUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.SensorUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.TitleUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.UpdateJobHelper;
import esthesis.services.dashboard.impl.service.DashboardService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseBroadcaster;
import jakarta.ws.rs.sse.SseEventSink;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory for creating {@link DashboardUpdateJob} instances.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class DashboardUpdateJobFactory {

	private final AuditUpdateJobHelper auditUpdateJobHelper;
	private final SensorUpdateJobHelper sensorUpdateJobHelper;
	private final SensorIconUpdateJobHelper sensorIconUpdateJobHelper;
	private final AboutUpdateJobHelper aboutUpdateJobHelper;
	private final CampaignsUpdateJobHelper campaignsUpdateJobHelper;
	private final DevicesLatestUpdateJobHelper devicesLatestUpdateJobHelper;
	private final DevicesLastSeenUpdateJobHelper devicesLastSeenUpdateJobHelper;
	private final DeviceMapUpdateJobHelper deviceMapUpdateJobHelper;
	private final DevicesStatusUpdateJobHelper devicesStatusUpdateJobHelper;
	private final NotesUpdateJobHelper notesUpdateJobHelper;
	private final SecurityStatsUpdateJobHelper securityStatsUpdateJobHelper;
	private final TitleUpdateJobHelper titleUpdateJobHelper;
	private final DashboardService dashboardService;
	private final Sse sse;
	private final ObjectMapper objectMapper;

	private Map<Class<? extends UpdateJobHelper<?>>, UpdateJobHelper<?>> getHelpers() {
		Map<Class<? extends UpdateJobHelper<?>>, UpdateJobHelper<?>> map = new HashMap<>();
		map.put(AboutUpdateJobHelper.class, aboutUpdateJobHelper);
		map.put(AuditUpdateJobHelper.class, auditUpdateJobHelper);
		map.put(CampaignsUpdateJobHelper.class, campaignsUpdateJobHelper);
		map.put(DeviceMapUpdateJobHelper.class, deviceMapUpdateJobHelper);
		map.put(DevicesLastSeenUpdateJobHelper.class, devicesLastSeenUpdateJobHelper);
		map.put(DevicesLatestUpdateJobHelper.class, devicesLatestUpdateJobHelper);
		map.put(DevicesStatusUpdateJobHelper.class, devicesStatusUpdateJobHelper);
		map.put(SensorUpdateJobHelper.class, sensorUpdateJobHelper);
		map.put(SensorIconUpdateJobHelper.class, sensorIconUpdateJobHelper);
		map.put(NotesUpdateJobHelper.class, notesUpdateJobHelper);
		map.put(SecurityStatsUpdateJobHelper.class, securityStatsUpdateJobHelper);
		map.put(TitleUpdateJobHelper.class, titleUpdateJobHelper);

		return map;
	}

	/**
	 * Creates a new {@link DashboardUpdateJob} instance.
	 *
	 * @param subscriptionId the id of the user subscription for this dashboard (a user may have
	 *                       multiple windows open for the same dashboard, each dashboard receives a
	 *                       unique subscription id).
	 * @param dashboardId    the id of the dashboard to subscribe to.
	 * @param sseEventSink   the {@link SseEventSink} to send updates to.
	 * @return a new {@link DashboardUpdateJob} instance.
	 */
	public DashboardUpdateJob create(String subscriptionId, String dashboardId,
		SseEventSink sseEventSink) {
		log.debug("Initialising job task for subscription '{}' for dashboard '{}'.", subscriptionId,
			dashboardId);
		// Prepare job data.
		SseBroadcaster sseBroadcaster = sse.newBroadcaster();
		sseBroadcaster.register(sseEventSink);
		DashboardEntity dashboardEntity = dashboardService.findByIdOptional(dashboardId).orElseThrow(
			() -> new QDoesNotExistException("Dashboard '{}' does not exist.", dashboardId));

		// Initialise helpers.
		getHelpers().forEach((k, v) -> v.init());

		// Create job.
		DashboardUpdateJob job = new DashboardUpdateJob(subscriptionId, sse, dashboardId,
			sseBroadcaster, dashboardEntity, objectMapper, getHelpers());

		// Return job.
		log.debug("Job task initialised for subscription '{}' for dashboard '{}'.", subscriptionId,
			dashboardId);
		return job;
	}
}
