package esthesis.service.dt.impl.service;

import esthesis.common.entity.CommandReplyEntity;
import esthesis.service.command.dto.ExecuteRequestScheduleInfoDTO;
import esthesis.service.command.entity.CommandRequestEntity;
import esthesis.service.command.resource.CommandSystemResource;
import esthesis.service.dt.dto.DTValueReplyDTO;
import esthesis.util.redis.RedisUtils;
import esthesis.util.redis.RedisUtils.KeyType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.awaitility.Awaitility;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static esthesis.common.AppConstants.REDIS_KEY_SUFFIX_TIMESTAMP;
import static esthesis.common.AppConstants.REDIS_KEY_SUFFIX_VALUE_TYPE;

@Slf4j
@Transactional
@ApplicationScoped
public class DTService {

	@Inject
	RedisUtils redisUtils;

	@Inject
	@RestClient
	CommandSystemResource commandSystemResource;


	/**
	 * Finds a specific value previously cached for a device.
	 *
	 * @param hardwareId  The hardware id to target.
	 * @param category    The category of the measurement.
	 * @param measurement The measurement to find.
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

	public String sendCommandAsync(CommandRequestEntity commandRequestEntity) {
		// Save the request and immediately return its correlation ID
		return  commandSystemResource.save(commandRequestEntity).getCorrelationId();
	}

	public List<CommandReplyEntity> sendCommandSync(CommandRequestEntity commandRequestEntity) {
		// Save the request and schedule its execution.
		ExecuteRequestScheduleInfoDTO requestScheduleInfo = commandSystemResource.save(commandRequestEntity);

		// Wait for replies to be collected.
		boolean allRepliesCollected = waitForReplies(requestScheduleInfo, 10000, 300);

		if (!allRepliesCollected) {
			log.warn("Timeout occurred while waiting for replies.");
		}

		// Collect and return the replies.
		return getReplies(requestScheduleInfo.getCorrelationId());
	}

	public List<CommandReplyEntity> getReplies(String correlationId){
		return  commandSystemResource.getReplies(correlationId);
	}

	/**
	 * Waits for the expected number of replies within a timeout.
	 *
	 * @param requestScheduleInfo Info about the scheduled request, including correlation ID and number of devices.
	 * @param timeout Maximum wait time in milliseconds.
	 * @param pollInterval Interval to check the condition in milliseconds.
	 * @return true if all replies are collected within the timeout, false if a timeout occurs.
	 */
	private boolean waitForReplies(ExecuteRequestScheduleInfoDTO requestScheduleInfo, long timeout, long pollInterval) {
		try {
			Awaitility.await()
				.atMost(timeout, TimeUnit.MILLISECONDS)
				.pollInterval(pollInterval, TimeUnit.MILLISECONDS)
				.until(() -> commandSystemResource.countCollectedReplies(
						requestScheduleInfo.getCorrelationId()
					) == requestScheduleInfo.getDevicesScheduled()
				);
			return true;
		} catch (org.awaitility.core.ConditionTimeoutException e) {
			log.warn("Awaitility timeout occurred while waiting for replies for correlationID: {}", requestScheduleInfo.getCorrelationId());
			return false;
		}
	}

}
