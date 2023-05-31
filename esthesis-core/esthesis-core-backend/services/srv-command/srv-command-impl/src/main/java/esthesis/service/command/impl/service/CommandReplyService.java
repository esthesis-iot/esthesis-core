package esthesis.service.command.impl.service;

import esthesis.common.entity.CommandReplyEntity;
import esthesis.service.common.BaseService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@ApplicationScoped
class CommandReplyService extends BaseService<CommandReplyEntity> {

	public List<CommandReplyEntity> findByCorrelationId(String correlationId) {
		return findByColumn("correlationId", correlationId);
	}

	/**
	 * Purges all replies older than the given duration.
	 *
	 * @param duration The duration, in days.
	 */
	public void purge(Optional<Integer> duration) {
		if (duration.isEmpty()) {
			deleteAll();
		} else {
			getRepository().delete(
				"createdOn <= ?1",
				Instant.now().minus(Duration.ofDays(duration.get())));
		}
	}

}
