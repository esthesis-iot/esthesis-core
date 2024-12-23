package esthesis.services.dashboard.impl.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.services.dashboard.impl.dto.DashboardUpdate;
import esthesis.services.dashboard.impl.job.helper.AboutUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.AuditUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.CampaignsUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.DevicesLastSeenUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.DevicesLatestUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.SensorIconUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.SensorUpdateJobHelper;
import esthesis.services.dashboard.impl.service.DashboardService;
import io.quarkus.arc.Unremovable;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseBroadcaster;
import jakarta.ws.rs.sse.SseEventSink;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Job for updating a dashboard. This job checks which elements a dashboard contains and updates the
 * dashboard accordingly by sending an event to the user's SSE event sink.
 * <p>
 * Note that since all data gathering for dashboards takes place via scheduled jobs, no active user
 * is present when those calls take place. For this reason, we need to use a 'system' resource REST
 * client. Effectively, security needs to be checked at source (i.e. before calling the 'system'
 * REST client, check if the user has the necessary permissions to access the underlying data).
 */
@Slf4j
@Dependent
@Unremovable
public class DashboardUpdateJob {

	@Inject
	DashboardService dashboardService;
	@Inject
	Sse sse;
	@Inject
	DevicesLastSeenUpdateJobHelper devicesLastSeenUpdateJobHelper;
	@Setter
	private String dashboardId;
	@Setter
	private SseEventSink sseEventSink;
	private SseBroadcaster sseBroadcaster;
	private DashboardEntity dashboardEntity;
	@Inject
	ObjectMapper objectMapper;
	// A local cache to maintain a hash of the last update message sent for each dashboard item.
	// This is used to prevent sending the same message multiple times when no updates exist.
	private final Cache<String, String> updateJobCache = Caffeine.newBuilder()
		.expireAfterWrite(Duration.ofHours(1)).build();

	@Inject
	AuditUpdateJobHelper auditUpdateJobHelper;
	@Inject
	@Named("SensorUpdateJobHelper")
	SensorUpdateJobHelper sensorUpdateJobHelper;
	@Inject
	@Named("SensorIconUpdateJobHelper")
	SensorIconUpdateJobHelper sensorIconUpdateJobHelper;
	@Inject
	AboutUpdateJobHelper aboutUpdateJobHelper;
	@Inject
	CampaignsUpdateJobHelper campaignsUpdateJobHelper;
	@Inject
	DevicesLatestUpdateJobHelper devicesLatestUpdateJobHelper;

	/**
	 * Broadcasts a dashboard update to the user's SSE event sink, while avoiding sending the same
	 * message multiple times.
	 *
	 * @param update the dashboard update to broadcast
	 * @param itemId the ID of the dashboard item
	 */
	private synchronized void broadcast(DashboardUpdate update, String itemId) {
		if (update != null) {
			try {
				String json = objectMapper.writeValueAsString(update);
				String jsonHash = DigestUtils.md5Hex(json);
				if (!jsonHash.equals(updateJobCache.getIfPresent(itemId))) {
					updateJobCache.put(itemId, jsonHash);
					sseBroadcaster.broadcast(sse.newEvent(json));
				}
			} catch (JsonProcessingException e) {
				log.error("Error parsing update for dashboard item with id '{}'.", itemId, e);
			}
		}
	}

	/**
	 * Hooks the SSE event sink to the broadcaster. This method should be called once during job
	 * initialization.
	 */
	public void hookSse() {
		sseBroadcaster = sse.newBroadcaster();
		sseBroadcaster.register(sseEventSink);
	}

	/**
	 * Sets the dashboard entity. This method should be called once during job initialization.
	 */
	public void setDashboardEntity() {
		dashboardEntity = dashboardService.findByIdOptional(dashboardId).orElseThrow(
			() -> new QDoesNotExistException("Dashboard with id '" + dashboardId + "' does not exist."));
	}

	/**
	 * Destroys the job task, releasing the SSE broadcaster.
	 */
	@PreDestroy
	public void destroy() {
		log.debug("Destroying job task for dashboard '{}'.", dashboardId);
		if (sseBroadcaster != null) {
			sseBroadcaster.close();
		}
		log.debug("Job task destroyed for dashboard '{}'.", dashboardId);
	}

	/**
	 * Executes the job task.
	 */
	public void execute() {
		System.out.println("Executing job for dashboard '" + dashboardId + "'.");
		dashboardEntity.getItems().stream().filter(DashboardItemDTO::isEnabled).forEach(item -> {
			switch (item.getType()) {
				case AUDIT:
					CompletableFuture.runAsync(
						() -> broadcast(auditUpdateJobHelper.refresh(dashboardEntity, item), item.getId()));
					break;
				case SENSOR:
					CompletableFuture.runAsync(
						() -> broadcast(sensorUpdateJobHelper.refresh(dashboardEntity, item), item.getId()));
					break;
				case SENSOR_ICON:
					CompletableFuture.runAsync(
						() -> broadcast(sensorIconUpdateJobHelper.refresh(dashboardEntity, item),
							item.getId()));
					break;
				case ABOUT:
					CompletableFuture.runAsync(
						() -> broadcast(aboutUpdateJobHelper.refresh(dashboardEntity, item), item.getId()));
					break;
				case CAMPAIGNS:
					CompletableFuture.runAsync(
						() -> broadcast(campaignsUpdateJobHelper.refresh(dashboardEntity, item), item.getId()));
					break;
				case DEVICES_LAST_SEEN:
					CompletableFuture.runAsync(
						() -> broadcast(devicesLastSeenUpdateJobHelper.refresh(dashboardEntity, item),
							item.getId()));
					break;
				case DEVICES_LATEST:
					CompletableFuture.runAsync(
						() -> broadcast(devicesLatestUpdateJobHelper.refresh(dashboardEntity, item),
							item.getId()));
					break;
				default:
					log.warn("Unknown dashboard item type '{}' for dashboard '{}'.", item.getType(),
						dashboardId);
			}
		});
	}

}
