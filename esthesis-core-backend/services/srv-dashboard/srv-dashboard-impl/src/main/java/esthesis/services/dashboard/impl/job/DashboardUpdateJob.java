package esthesis.services.dashboard.impl.job;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.common.exception.QDoesNotExistException;
import esthesis.core.common.AppConstants.Dashboard.Type;
import esthesis.core.common.AppConstants.Security.Category;
import esthesis.core.common.AppConstants.Security.Operation;
import esthesis.service.audit.resource.AuditSystemResource;
import esthesis.service.dashboard.dto.DashboardItemDTO;
import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.service.security.resource.SecuritySystemResource;
import esthesis.services.dashboard.impl.dto.DashboardUpdate;
import esthesis.services.dashboard.impl.dto.config.DashboardItemAuditConfiguration;
import esthesis.services.dashboard.impl.dto.config.DashboardItemSensorConfiguration;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateAudit;
import esthesis.services.dashboard.impl.dto.update.DashboardUpdateSensor;
import esthesis.services.dashboard.impl.service.DashboardService;
import esthesis.util.redis.RedisUtils;
import esthesis.util.redis.RedisUtils.KeyType;
import io.quarkus.arc.Unremovable;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseBroadcaster;
import jakarta.ws.rs.sse.SseEventSink;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.microprofile.rest.client.inject.RestClient;

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
	@RestClient
	SecuritySystemResource securitySystemResource;
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
	RedisUtils redisUtils;

	// A map representing security checks. To not repeat security checks each time the value of a
	// dashboard item is refreshed, the result of the first security check for the specific type
	// of dashboard item is stored in this map. For as long as this job is active, the security
	// check result is considered valid (i.e. if user's permissions change, the updated security
	// permissions will only be taken into account the next time the user will view the dashboard).
	//
	// The key of the map is a unique entry constructed for the specific dashboard item being checked,
	// while the value is a boolean representing the result of the security check.
	private final Map<String, Boolean> securityChecks = new HashMap<>();

	// Data services.
	@Inject
	@RestClient
	AuditSystemResource auditSystemResource;

	private <C> C getConfig(Class<C> configurationClass, DashboardItemDTO item)
	throws JsonProcessingException {
		return objectMapper.readValue(item.getConfiguration(), configurationClass);
	}

	private boolean checkSecurity(Category category, Operation operation, String hardwareId) {
		String securityKey = String.join(":", category.toString(), operation.toString(), hardwareId,
			dashboardEntity.getOwnerId().toHexString());
		if (securityChecks.containsKey(securityKey) && Boolean.FALSE.equals(
			securityChecks.get(securityKey))) {
			return false;
		} else if (!securityChecks.containsKey(securityKey)) {
			Boolean check = securitySystemResource.isPermitted(category, operation, hardwareId,
				dashboardEntity.getOwnerId());
			securityChecks.put(securityKey, check);
		}
		return Boolean.TRUE.equals(securityChecks.get(securityKey));
	}

	private DashboardUpdateAudit refreshAudit(DashboardItemDTO item) throws JsonProcessingException {
		// Get item configuration & security checks.
		DashboardItemAuditConfiguration config = getConfig(DashboardItemAuditConfiguration.class, item);
		if (!checkSecurity(Category.AUDIT, Operation.READ, "")) {
			return null;
		}

		// Get audit entries.
		return DashboardUpdateAudit.builder()
			.id(item.getId())
			.type(Type.AUDIT)
			.auditEntries(
				auditSystemResource.find(config.getEntries()).getContent().stream()
					.map(auditEntity -> Pair.of(auditEntity.getCreatedBy(), auditEntity.getMessage()))
					.toList())
			.build();
	}

	private DashboardUpdateSensor refreshSensor(DashboardItemDTO item)
	throws JsonProcessingException {
		// Get item configuration & security checks.
		DashboardItemSensorConfiguration config = getConfig(DashboardItemSensorConfiguration.class,
			item);
		if (StringUtils.isBlank(config.getHardwareId()) || StringUtils.isBlank(
			config.getMeasurement())) {
			return null;
		}
		if (!checkSecurity(Category.DEVICE, Operation.READ, config.getHardwareId())) {
			return null;
		}

		// Get sensor value.
		return DashboardUpdateSensor.builder()
			.id(item.getId())
			.type(Type.SENSOR)
			.hardwareId(config.getHardwareId())
			.measurement(config.getMeasurement())
			.value(redisUtils.getFromHash(KeyType.ESTHESIS_DM, config.getHardwareId(),
				config.getMeasurement()))
			.build();
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
		final List<DashboardUpdate> dashboardUpdates = new ArrayList<>();
		dashboardEntity.getItems().forEach(item -> {
			try {
				switch (item.getType()) {
					case AUDIT:
						Optional.ofNullable(refreshAudit(item)).ifPresent(dashboardUpdates::add);
						break;
					case SENSOR:
						Optional.ofNullable(refreshSensor(item)).ifPresent(dashboardUpdates::add);
						break;
					case ABOUT:
						Optional.ofNullable(refreshAbout(item)).ifPresent(dashboardUpdates::add);
						break;
					default:
						log.warn("Unknown dashboard item type '{}' for dashboard '{}'.", item.getType(),
							dashboardId);
				}
			} catch (JsonProcessingException e) {
				log.error("Could not refresh audit for dashboard item '{}'.", item, e);
			}
		});

		// Create a JSON representation of the dashboard update data and send it to the user via SSE.
		try {
			String json = objectMapper.writeValueAsString(dashboardUpdates);
			log.trace("Sending dashboard update for dashboard '{}': {}", dashboardId, json);
			sseBroadcaster.broadcast(sse.newEvent(json));
		} catch (Exception e) {
			log.error("Could not serialise dashboard update for dashboard '{}'.", dashboardId, e);
		}
	}

}
