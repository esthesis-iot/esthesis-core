package esthesis.service.command.impl.service;

import esthesis.common.entity.CommandReplyEntity;
import esthesis.service.common.BaseService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
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
	public boolean deleteById(String deviceId) {
		return super.deleteById(deviceId);
	}

	@Override
	public long countByColumn(String column, Object value) {
		return super.countByColumn(column, value);
	}

	@Override
	public long deleteByColumn(String columnName, Object value) {
		return super.deleteByColumn(columnName, value);
	}
}
