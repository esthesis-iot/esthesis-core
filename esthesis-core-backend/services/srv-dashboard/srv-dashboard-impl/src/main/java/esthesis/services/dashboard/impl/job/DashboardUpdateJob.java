package esthesis.services.dashboard.impl.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.services.dashboard.impl.dto.DashboardUpdate;
import esthesis.services.dashboard.impl.job.helper.AboutUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.AuditUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.CampaignsUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.DatetimeUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.DeviceMapUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.DevicesLastSeenUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.DevicesLatestUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.DevicesStatusUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.ImageUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.NotesUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.SecurityStatsUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.SensorIconUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.SensorUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.TitleUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.UpdateJobHelper;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseBroadcaster;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class DashboardUpdateJob {

	private final String subscriptionId;
	private final Sse sse;
	private final String dashboardId;
	private final SseBroadcaster sseBroadcaster;
	private final DashboardEntity dashboardEntity;
	private final ObjectMapper objectMapper;
	private final Map<Class<? extends UpdateJobHelper<?>>, UpdateJobHelper<?>> helpers;

	// A local cache to maintain a hash of the last update message sent for each dashboard item.
	// This is used to prevent sending the same message multiple times when no updates exist.
	// The cache has a default expiry of 1 hour, so at least once per hour every dashboard item
	// will receive an update regardless of whether the data has changed or not.
	private final Cache<String, String> updateJobCache = Caffeine.newBuilder()
		.expireAfterWrite(Duration.ofHours(1)).build();

	// A pool of threads, to make sure that under unfavourable circumstances we do not exhaust
	// system resources.
	private final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
		10, // Core pool size.
		50, // Maximum pool size.
		5000L, TimeUnit.MILLISECONDS, // Keep-alive time.
		new ArrayBlockingQueue<>(50), // Task queue capacity.
		new ThreadPoolExecutor.AbortPolicy() // Default rejection policy.
	);

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
	 * Destroys the job task, releasing the SSE broadcaster.
	 */
	public void destroy() {
		log.debug("Destroying job task for subscription '{}' for dashboard '{}'.", subscriptionId,
			dashboardId);
		if (sseBroadcaster != null) {
			sseBroadcaster.close();
		}
		log.debug("Job task destroyed for subscription '{}' for dashboard '{}'.", subscriptionId,
			dashboardId);
	}

	/**
	 * Executes the job task.
	 */
	public void execute() {
		log.debug("Executing job for subscription '{}' for dashboard '{}'.", subscriptionId,
			dashboardId);
		dashboardEntity.getItems().stream().filter(DashboardItemDTO::isEnabled).forEach(item -> {
			// Uncomment to simulate a slow update.
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			switch (item.getType()) {
				case ABOUT:
					CompletableFuture.runAsync(
						() -> broadcast(((AboutUpdateJobHelper) helpers.get(AboutUpdateJobHelper.class))
							.refresh(dashboardEntity, item), item.getId()), threadPool);
					break;
				case AUDIT:
					CompletableFuture.runAsync(
						() -> broadcast(((AuditUpdateJobHelper) helpers.get(AuditUpdateJobHelper.class))
							.refresh(dashboardEntity, item), item.getId()), threadPool);
					break;
				case CAMPAIGNS:
					CompletableFuture.runAsync(
						() -> broadcast(((CampaignsUpdateJobHelper) helpers.get(CampaignsUpdateJobHelper.class))
							.refresh(dashboardEntity, item), item.getId()), threadPool);
					break;
				case DATETIME:
					CompletableFuture.runAsync(
						() -> broadcast(((DatetimeUpdateJobHelper) helpers.get(DatetimeUpdateJobHelper.class))
							.refresh(dashboardEntity, item), item.getId()), threadPool);
					break;
				case DEVICE_MAP:
					CompletableFuture.runAsync(
						() -> broadcast(
							((DeviceMapUpdateJobHelper) helpers.get(DeviceMapUpdateJobHelper.class))
								.refresh(dashboardEntity, item), item.getId()), threadPool);
					break;
				case DEVICES_LAST_SEEN:
					CompletableFuture.runAsync(
						() -> broadcast(
							((DevicesLastSeenUpdateJobHelper) helpers.get(DevicesLastSeenUpdateJobHelper.class))
								.refresh(dashboardEntity, item), item.getId()), threadPool);
					break;
				case DEVICES_LATEST:
					CompletableFuture.runAsync(
						() -> broadcast(
							((DevicesLatestUpdateJobHelper) helpers.get(DevicesLatestUpdateJobHelper.class))
								.refresh(dashboardEntity, item), item.getId()), threadPool);
					break;
				case DEVICES_STATUS:
					CompletableFuture.runAsync(
						() -> broadcast(
							((DevicesStatusUpdateJobHelper) helpers.get(DevicesStatusUpdateJobHelper.class))
								.refresh(dashboardEntity, item), item.getId()), threadPool);
					break;
				case IMAGE:
					CompletableFuture.runAsync(
						() -> broadcast(
							((ImageUpdateJobHelper) helpers.get(ImageUpdateJobHelper.class))
								.refresh(dashboardEntity, item), item.getId()), threadPool);
					break;
				case NOTES:
					CompletableFuture.runAsync(
						() -> broadcast(
							((NotesUpdateJobHelper) helpers.get(NotesUpdateJobHelper.class))
								.refresh(dashboardEntity, item), item.getId()), threadPool);
					break;
				case SECURITY_STATS:
					CompletableFuture.runAsync(
						() -> broadcast(
							((SecurityStatsUpdateJobHelper) helpers.get(SecurityStatsUpdateJobHelper.class))
								.refresh(dashboardEntity, item), item.getId()), threadPool);
					break;
				case SENSOR:
					CompletableFuture.runAsync(
						() -> broadcast(((SensorUpdateJobHelper) helpers.get(SensorUpdateJobHelper.class))
							.refresh(dashboardEntity, item), item.getId()), threadPool);
					break;
				case SENSOR_ICON:
					CompletableFuture.runAsync(
						() -> broadcast(
							((SensorIconUpdateJobHelper) helpers.get(SensorIconUpdateJobHelper.class))
								.refresh(dashboardEntity, item), item.getId()), threadPool);
					break;
				case TITLE:
					CompletableFuture.runAsync(
						() -> broadcast(
							((TitleUpdateJobHelper) helpers.get(TitleUpdateJobHelper.class))
								.refresh(dashboardEntity, item), item.getId()), threadPool);
					break;
				default:
					log.warn("Unknown dashboard item type '{}' for subscription '{}' for dashboard '{}'.",
						item.getType(), subscriptionId, dashboardId);
			}
		});
	}

}
