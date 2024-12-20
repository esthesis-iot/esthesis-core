package esthesis.services.dashboard.impl.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.services.dashboard.impl.dto.DashboardUpdate;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateCampaigns;
import esthesis.services.dashboard.impl.job.helper.AboutUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.AuditUpdateJobHelper;
import esthesis.services.dashboard.impl.job.helper.CampaignsUpdateJobHelper;
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
import java.util.concurrent.CompletableFuture;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

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
	@Setter
	private String dashboardId;
	@Setter
	private SseEventSink sseEventSink;
	private SseBroadcaster sseBroadcaster;
	private DashboardEntity dashboardEntity;
	@Inject
	ObjectMapper objectMapper;

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
					CompletableFuture.runAsync(() -> {
						try {
							DashboardUpdate update = auditUpdateJobHelper.refresh(dashboardEntity, item);
							if (update != null) {
								String json = objectMapper.writeValueAsString(update);
								sseBroadcaster.broadcast(sse.newEvent(json));
							}
						} catch (JsonProcessingException e) {
							log.error("Could not refresh audit for dashboard item '{}'.", item, e);
						}
					});
					break;
				case SENSOR:
					CompletableFuture.runAsync(() -> {
						try {
							DashboardUpdate update = sensorUpdateJobHelper.refresh(dashboardEntity, item);
							if (update != null) {
								String json = objectMapper.writeValueAsString(update);
								sseBroadcaster.broadcast(sse.newEvent(json));
							}
						} catch (JsonProcessingException e) {
							log.error("Could not refresh sensor for dashboard item '{}'.", item, e);
						}
					});
					break;
				case SENSOR_ICON:
					CompletableFuture.runAsync(() -> {
						try {
							DashboardUpdate update = sensorIconUpdateJobHelper.refresh(dashboardEntity, item);
							if (update != null) {
								String json = objectMapper.writeValueAsString(update);
								sseBroadcaster.broadcast(sse.newEvent(json));
							}
						} catch (JsonProcessingException e) {
							log.error("Could not refresh sensor icon for dashboard item '{}'.", item, e);
						}
					});
					break;
				case ABOUT:
					CompletableFuture.runAsync(() -> {
						try {
							DashboardUpdate update = aboutUpdateJobHelper.refresh(dashboardEntity, item);
							if (update != null) {
								String json = objectMapper.writeValueAsString(update);
								sseBroadcaster.broadcast(sse.newEvent(json));
							}
						} catch (JsonProcessingException e) {
							log.error("Could not refresh about for dashboard item '{}'.", item, e);
						}
					});
					break;
				case CAMPAIGNS:
					CompletableFuture.runAsync(() -> {
						try {
							DashboardUpdateCampaigns update = campaignsUpdateJobHelper.refresh(dashboardEntity,
								item);
							if (update != null) {
								String json = objectMapper.writeValueAsString(update);
								sseBroadcaster.broadcast(sse.newEvent(json));
							}
						} catch (JsonProcessingException e) {
							log.error("Could not refresh campaigns for dashboard item '{}'.", item, e);
						}
					});
					break;
				default:
					log.warn("Unknown dashboard item type '{}' for dashboard '{}'.", item.getType(),
						dashboardId);
			}
		});

//		// Create a JSON representation of the dashboard update data and send it to the user via SSE.
//		try {
//			String json = objectMapper.writeValueAsString(dashboardUpdates);
//			log.trace("Sending dashboard update for dashboard '{}': {}", dashboardId, json);
//			sseBroadcaster.broadcast(sse.newEvent(json));
//		} catch (Exception e) {
//			log.error("Could not serialise dashboard update for dashboard '{}'.", dashboardId, e);
//		}
	}

}
