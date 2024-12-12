package esthesis.services.dashboard.impl.job;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.spi.CDI;
import jakarta.ws.rs.sse.SseEventSink;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

/**
 * Factory for creating {@link DashboardUpdateJob} instances.
 */
@Slf4j
@ApplicationScoped
public class DashboardUpdateJobFactory {

	/**
	 * Creates a new {@link DashboardUpdateJob} instance.
	 *
	 * @param dashboardId  the id of the dashboard to subscribe to.
	 * @param sseEventSink the {@link SseEventSink} to send updates to.
	 * @return a new {@link DashboardUpdateJob} instance.
	 */
	public DashboardUpdateJob create(String dashboardId, SseEventSink sseEventSink) {
		log.debug("Initialising job task for dashboard '{}'.", dashboardId);
		DashboardUpdateJob job = CDI.current().select(DashboardUpdateJob.class).get();
		job.setDashboardId(dashboardId);
		job.setScheduledOn(Instant.now());
		job.setSseEventSink(sseEventSink);
		job.hookSse();
		job.setDashboardEntity();
		log.debug("Job task initialised for dashboard '{}'.", dashboardId);

		return job;
	}
}
