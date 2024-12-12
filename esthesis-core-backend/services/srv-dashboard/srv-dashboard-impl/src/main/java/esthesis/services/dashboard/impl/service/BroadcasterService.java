package esthesis.services.dashboard.impl.service;

import static esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Action.REFRESHSUB;
import static esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Action.UNSUB;
import static esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.SMALLRYE_KAFKA_BROADCAST_CHANNEL_IN;

import esthesis.services.dashboard.impl.job.DashboardUpdateJob;
import esthesis.services.dashboard.impl.job.DashboardUpdateJobFactory;
import esthesis.util.kafka.notifications.common.AppMessage;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Component;
import io.opentelemetry.context.Scope;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.Scheduler;
import io.quarkus.security.identity.SecurityIdentity;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.reactive.messaging.TracingMetadata;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.sse.SseEventSink;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.reactive.messaging.Incoming;

/**
 * Service for broadcasting dashboard updates to users.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class BroadcasterService {

	// The scheduler keeping track of the dashboard update jobs.
	private final Scheduler scheduler;
	// The factory for creating dashboard update jobs for a user and dashboard.
	private final DashboardUpdateJobFactory dashboardUpdateJobFactory;
	// A tracker to be able to identify stale jobs. The key of the map is the job identity and the
	// value is the time the job was initialised. Active dashboards should send periodic keep-alive
	// messages to keep their underlying update job active.
	private Map<String, Instant> jobsTracker = new ConcurrentHashMap<>();
	private final DashboardService dashboardService;
	private final SecurityIdentity securityIdentity;

	/**
	 * Cleans up all scheduled jobs when the service is destroyed.
	 */
	@PreDestroy
	void cleanup() {
		log.debug("Cleaning up scheduled jobs.");
		scheduler.getScheduledJobs().forEach(job -> {
			log.debug("Cleaning up job '{}'.", job.getId());
			scheduler.unscheduleJob(job.getId());
		});
		log.debug("Scheduled jobs cleaned up.");
	}

	/**
	 * Unregisters a user from receiving updates for a dashboard.
	 *
	 * @param dashboardId    the id of the dashboard.
	 * @param subscriptionId the id of the subscription sent by the client.
	 */
	private void unregister(String dashboardId, String subscriptionId) {
		log.debug("Unscheduling job for dashboard '{}'.", dashboardId);
		if (scheduler.getScheduledJob(subscriptionId) != null) {
			scheduler.unscheduleJob(subscriptionId);
			jobsTracker.remove(subscriptionId);
			log.debug("Job unscheduled for dashboard '{}'.", dashboardId);
		} else {
			log.debug("Could not find a job for dashboard '{}'.", dashboardId);
		}
	}

	private void refreshSub(String subscriptionId) {
		log.debug("Refreshing subscription '{}'.", subscriptionId);
		if (scheduler.getScheduledJob(subscriptionId) != null) {
			jobsTracker.put(subscriptionId, Instant.now());
			log.debug("Subscription '{}' refreshed.", subscriptionId);
		} else {
			log.debug("Could not find subscription '{}'.", subscriptionId);
		}
	}

	/**
	 * Checks for stale jobs and unschedules them. A job is considered stale if it has not been
	 * refreshed for more than an hour.
	 */
	@Scheduled(every = "5m")
	void checkStaleJobs() {
		log.trace("Checking for stale jobs.");
		for (Map.Entry<String, Instant> entry : new HashMap<>(jobsTracker).entrySet()) {
			if (entry.getValue().isBefore(Instant.now().minus(1, ChronoUnit.HOURS))) {
				log.debug("Job '{}' is stale, unscheduling.", entry.getKey());
				scheduler.unscheduleJob(entry.getKey());
				jobsTracker.remove(entry.getKey());
			}
		}
		log.trace("Stale jobs checked.");
	}

	/**
	 * Handles Kafka messages for unsubscribing users from dashboards.
	 *
	 * @param msg the Kafka message.
	 * @return a completion stage.
	 */
	@Blocking
	@Incoming(SMALLRYE_KAFKA_BROADCAST_CHANNEL_IN)
	CompletionStage<Void> onMessage(KafkaRecord<String, AppMessage> msg) {
		log.trace("Processing Kafka application message '{}'", msg);

		// Process the message.
		try (Scope scope = msg.getMetadata().get(TracingMetadata.class)
			.map(tm -> tm.getCurrentContext().makeCurrent())
			.orElse(io.opentelemetry.context.Context.current().makeCurrent())) {
			if (msg.getPayload().getComponent() == Component.DASHBOARD) {
				if (msg.getPayload().getAction() == UNSUB) {
					String username = msg.getKey();
					String dashboardId = msg.getPayload().getTargetId();
					unregister(username, dashboardId);
				} else if (msg.getPayload().getAction() == REFRESHSUB) {
					String subscriptionId = msg.getPayload().getTargetId();
					refreshSub(subscriptionId);
				}
			}
		} catch (Exception e) {
			log.warn("Could not handle Kafka message '{}'.", msg, e);
		}

		return msg.ack();
	}

	/**
	 * Registers a user to receive updates for a dashboard.
	 *
	 * @param dashboardId    the id of the dashboard.
	 * @param subscriptionId the id of the subscription sent by the client.
	 * @param sseEventSink   the event sink to send updates to.
	 */
	public void register(String dashboardId, String subscriptionId,
		SseEventSink sseEventSink) {
		log.debug("Scheduling job for dashboard '{}, with subscription id '{}'.", dashboardId,
			subscriptionId);

		dashboardService.findAllForCurrentUser().stream()
			.filter(dashboardEntity -> dashboardEntity.getId().toHexString().equals(dashboardId))
			.findFirst().ifPresentOrElse(dashboardEntity -> {
				DashboardUpdateJob job = dashboardUpdateJobFactory.create(dashboardId, sseEventSink);
				scheduler.newJob(subscriptionId)
					.setInterval(dashboardEntity.getUpdateInterval() + "s")
					.setTask(executionContext -> job.execute()).schedule();
				jobsTracker.put(subscriptionId, Instant.now());
				log.debug("Job scheduled for dashboard '{}', with subscription id '{}'.", dashboardId,
					subscriptionId);
			}, () -> log.debug("Could not find a dashboard subscription with id '{}'.", subscriptionId));
	}

}
