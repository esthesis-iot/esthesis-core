package esthesis.services.dashboard.impl.service;

import static esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Action.REFRESHSUB;
import static esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Action.UNSUB;
import static esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.SMALLRYE_KAFKA_BROADCAST_CHANNEL_IN;

import esthesis.service.dashboard.entity.DashboardEntity;
import esthesis.services.dashboard.impl.job.DashboardUpdateJob;
import esthesis.services.dashboard.impl.job.DashboardUpdateJobFactory;
import esthesis.util.kafka.notifications.common.AppMessage;
import esthesis.util.kafka.notifications.common.KafkaNotificationsConstants.Component;
import io.opentelemetry.context.Scope;
import io.quarkus.scheduler.Scheduled;
import io.quarkus.scheduler.Scheduler;
import io.smallrye.common.annotation.Blocking;
import io.smallrye.reactive.messaging.TracingMetadata;
import io.smallrye.reactive.messaging.kafka.KafkaRecord;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.sse.SseEventSink;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.microprofile.reactive.messaging.Incoming;

/**
 * Service for broadcasting dashboard updates to users.
 */
@Slf4j
@ApplicationScoped
@RequiredArgsConstructor
public class BroadcasterService {

	private final DashboardService dashboardService;
	// The scheduler executing the dashboard update jobs.
	private final Scheduler scheduler;
	// The factory for creating dashboard update jobs for a user and dashboard.
	private final DashboardUpdateJobFactory dashboardUpdateJobFactory;
	// A tracker to be able to identify stale jobs:
	// - The key is the job identity.
	// - The value is a pair with the job instance and the time the job was initialised.
	// Active dashboards should send periodic keep-alive messages to keep their respective update
	// job active.
	private final Map<String, Pair<DashboardUpdateJob, Instant>> subscriptionsTracker =
		new ConcurrentHashMap<>();

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
	 * @param subscriptionId the id of the subscription sent by the client.
	 */
	private void unregister(String subscriptionId) {
		log.debug("Unscheduling subscription '{}'.", subscriptionId);
		if (scheduler.getScheduledJob(subscriptionId) != null) {
			scheduler.unscheduleJob(subscriptionId);
			subscriptionsTracker.get(subscriptionId).getLeft().destroy();
			subscriptionsTracker.remove(subscriptionId);
			log.debug("Unscheduled subscription '{}'.", subscriptionId);
		} else {
			log.debug("Could not find subscription '{}' to unschedule it.", subscriptionId);
		}
	}

	/**
	 * Refreshes a subscription to keep the underlying job alive.
	 *
	 * @param subscriptionId the id of the subscription sent by the client.
	 */
	private void refreshSub(String subscriptionId) {
		log.debug("Refreshing subscription '{}'.", subscriptionId);
		if (scheduler.getScheduledJob(subscriptionId) != null) {
			subscriptionsTracker.replace(subscriptionId,
				ImmutablePair.of(subscriptionsTracker.get(subscriptionId).getLeft(), Instant.now()));
			log.debug("Subscription '{}' refreshed.", subscriptionId);
		} else {
			log.debug("Could not find subscription '{}' to refresh it.", subscriptionId);
		}
	}

	/**
	 * Checks for stale jobs and unschedules them. A job is considered stale if it has not been
	 * refreshed for more than 5 minutes (by default, dashboards send a keep-alive every 1 minute).
	 */
	@Scheduled(every = "1m")
	void checkStaleJobs() {
		log.trace("Checking for stale jobs.");
		for (Map.Entry<String, Pair<DashboardUpdateJob, Instant>> entry :
			new HashMap<>(subscriptionsTracker).entrySet()) {
			if (entry.getValue().getRight().isBefore(Instant.now().minus(15, ChronoUnit.MINUTES))) {
				log.debug("Job '{}' is stale, unscheduling.", entry.getKey());
				unregister(entry.getKey());
			}
		}
		log.trace("Stale jobs checked.");
	}

	/**
	 * Handles Kafka messages for unsubscribing users from dashboards, as well as refreshing
	 * subscriptions (i.e. to keep the underlying job alive).
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
					String dashboardId = msg.getPayload().getTargetId();
					unregister(dashboardId);
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

		// Find own dashboards and shared dashboards.
		List<DashboardEntity> dashboards = dashboardService.findAllForCurrentUser();
		dashboards.addAll(dashboardService.findShared());

		// Find the dashboard requested by the user.
		dashboards.stream()
			.filter(dashboardEntity -> dashboardEntity.getId().toHexString().equals(dashboardId))
			.findFirst().ifPresentOrElse(dashboardEntity -> {
				DashboardUpdateJob job = dashboardUpdateJobFactory.create(subscriptionId, dashboardId,
					sseEventSink);
				subscriptionsTracker.put(subscriptionId, ImmutablePair.of(job, Instant.now()));
				scheduler.newJob(subscriptionId)
					.setInterval(dashboardEntity.getUpdateInterval() + "s")
					.setTask(executionContext -> subscriptionsTracker.get(subscriptionId).getLeft().execute())
					.schedule();
				log.debug("Job scheduled for dashboard '{}', with subscription id '{}'.", dashboardId,
					subscriptionId);
			}, () -> log.debug("Could not find a dashboard with id '{}'.", subscriptionId));
	}

}
