package esthesis.service.command.impl.service;

import esthesis.service.command.entity.CommandRequestEntity;
import esthesis.service.common.BaseService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Transactional
@ApplicationScoped
class  CommandRequestService extends BaseService<CommandRequestEntity> {

	/**
	 * Purges all commands older than the given duration.
	 *
	 * @param duration The duration, in days.
	 */
	public void purge(int duration) {
		if (duration == 0) {
			deleteAll();
		} else {
			getRepository().delete(
				"createdOn <= ?1",
				Instant.now().minus(Duration.ofDays(duration)));
		}
	}

}
