package esthesis.service.command.impl.service;

import esthesis.service.command.entity.CommandRequestEntity;
import esthesis.service.common.BaseService;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing command requests.
 */
@Slf4j
@Transactional
@ApplicationScoped
class CommandRequestService extends BaseService<CommandRequestEntity> {

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

	@Override
	public CommandRequestEntity save(CommandRequestEntity entity) {
		return super.save(entity);
	}

	@Override
	public CommandRequestEntity findById(String id) {
		return super.findById(id);
	}

	@Override
	public Page<CommandRequestEntity> find(Pageable pageable) {
		return super.find(pageable);
	}

	@Override
	public boolean deleteById(String deviceId) {
		return super.deleteById(deviceId);
	}

}
