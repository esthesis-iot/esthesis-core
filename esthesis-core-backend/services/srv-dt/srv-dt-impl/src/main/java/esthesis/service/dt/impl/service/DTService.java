package esthesis.service.dt.impl.service;

import static esthesis.core.common.AppConstants.REDIS_KEY_SUFFIX_TIMESTAMP;
import static esthesis.core.common.AppConstants.REDIS_KEY_SUFFIX_VALUE_TYPE;

import esthesis.service.command.dto.ExecuteRequestScheduleInfoDTO;
import esthesis.core.common.entity.CommandReplyEntity;
import esthesis.service.command.entity.CommandRequestEntity;
import esthesis.service.command.resource.CommandSystemResource;
import esthesis.service.dt.dto.DTValueReplyDTO;
import esthesis.util.redis.RedisUtils;
import esthesis.util.redis.RedisUtils.KeyType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.awaitility.Awaitility;
import org.bson.Document;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Service for handling DT operations.
 */
@Slf4j
@Transactional
@ApplicationScoped
public class DTService {

	@Inject
	RedisUtils redisUtils;

	@Inject
	@RestClient
	CommandSystemResource commandSystemResource;

	@ConfigProperty(name = "esthesis.dt-api.timeout-in-ms")
	Integer timeoutMs;

	@ConfigProperty(name = "esthesis.dt-api.poll-interval-in-ms")
	Integer pollInvervalMs;

	/**
	 * Finds a specific value previously cached for a device.
	 *
	 * @param hardwareId  The hardware id to target.
	 * @param category    The category of the measurement.
	 * @param measurement The measurement to find.
	 * @return The value if found, otherwise null.
	 */
	public DTValueReplyDTO find(String hardwareId, String category, String measurement) {
		String value = redisUtils.getFromHash(
			KeyType.ESTHESIS_DM, hardwareId, String.join(".", category, measurement));

		if (StringUtils.isNotBlank(value)) {
			String valueType = redisUtils.getFromHash(KeyType.ESTHESIS_DM, hardwareId,
				String.join(".", category, measurement, REDIS_KEY_SUFFIX_VALUE_TYPE));
			Instant valueTimestamp = Instant.parse(redisUtils.getFromHash(KeyType.ESTHESIS_DM, hardwareId,
				String.join(".", category, measurement, REDIS_KEY_SUFFIX_TIMESTAMP)));

			return new DTValueReplyDTO()
				.setHardwareId(hardwareId)
				.setCategory(category)
				.setMeasurement(measurement)
				.setValueType(valueType)
				.setRecordedAt(valueTimestamp)
				.setValue(value);
		} else {
			return null;
		}
	}

	/**
	 * Finds all values previously cached for a device for a specific category of measurements.
	 *
	 * @param hardwareId The hardware id to target.
	 * @param category   The category of the measurements to return.
	 * @return A list of values if found, otherwise an empty list.
	 */
	public List<DTValueReplyDTO> findAll(String hardwareId, String category) {
		List<DTValueReplyDTO> values = new ArrayList<>();
		Map<String, String> keys = redisUtils.getHash(KeyType.ESTHESIS_DM, hardwareId);
		keys.forEach((key, value) -> {
			if (key.startsWith(category)) {
				String[] parts = key.split("\\.");
				if (parts.length == 2) {
					values.add(
						new DTValueReplyDTO()
							.setHardwareId(hardwareId)
							.setCategory(category)
							.setMeasurement(parts[1])
							.setValueType(
								redisUtils.getFromHash(KeyType.ESTHESIS_DM, hardwareId,
									String.join(".", category, parts[1], REDIS_KEY_SUFFIX_VALUE_TYPE)))
							.setRecordedAt(
								Instant.parse(redisUtils.getFromHash(KeyType.ESTHESIS_DM, hardwareId,
									String.join(".", category, parts[1], REDIS_KEY_SUFFIX_TIMESTAMP))))
							.setValue(value));
				}
			}
		});

		return values;
	}

	/**
	 * Saves a command request to the command system.
	 *
	 * @param commandRequestEntity The command request to save.
	 * @return The schedule info for the saved request.
	 */
	public ExecuteRequestScheduleInfoDTO saveCommandRequest(
		CommandRequestEntity commandRequestEntity) {
		return commandSystemResource.save(commandRequestEntity);
	}

	/**
	 * Gets the replies for a specific correlation ID.
	 *
	 * @param correlationId The correlation ID to get replies for.
	 * @return A list of replies.
	 */
	public List<Document> getReplies(String correlationId) {
		return commandSystemResource.getReplies(correlationId).stream()
			.map(CommandReplyEntity::asDocument).toList();
	}

	/**
	 * Waits for the expected number of replies within a timeout of 10s with a poll interval of
	 * 300ms.
	 *
	 * @param requestScheduleInfo Info about the scheduled request, including correlation ID and
	 *                            number of devices.
	 * @return A List of replies
	 */
	public List<Document> waitAndGetReplies(ExecuteRequestScheduleInfoDTO requestScheduleInfo) {
		// Wait for replies to be collected.
		boolean allRepliesCollected = waitForReplies(requestScheduleInfo);

		if (!allRepliesCollected) {
			log.warn("Timeout occurred while waiting for replies.");
		}

		// Collect and return the replies.
		return getReplies(requestScheduleInfo.getCorrelationId());
	}

	/**
	 * Waits for replies to a specific command up to a timeout.
	 *
	 * @param requestScheduleInfo The request schedule info.
	 * @return True if all replies were collected, otherwise false.
	 */
	private boolean waitForReplies(ExecuteRequestScheduleInfoDTO requestScheduleInfo) {
		try {
			Awaitility.await()
				.atMost(timeoutMs, TimeUnit.MILLISECONDS)
				.pollInterval(pollInvervalMs, TimeUnit.MILLISECONDS)
				.until(() -> commandSystemResource.countCollectedReplies(
						requestScheduleInfo.getCorrelationId()
					) == requestScheduleInfo.getDevicesScheduled()
				);
			return true;
		} catch (org.awaitility.core.ConditionTimeoutException e) {
			log.warn("Awaitility timeout occurred while waiting for replies for correlationID: {}",
				requestScheduleInfo.getCorrelationId());
			return false;
		}
	}
}
