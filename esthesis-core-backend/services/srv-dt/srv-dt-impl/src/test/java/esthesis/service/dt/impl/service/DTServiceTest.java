package esthesis.service.dt.impl.service;

import esthesis.common.avro.CommandType;
import esthesis.common.avro.ExecutionType;
import esthesis.service.command.dto.ExecuteRequestScheduleInfoDTO;
import esthesis.service.command.entity.CommandRequestEntity;
import esthesis.service.command.resource.CommandSystemResource;
import esthesis.service.dt.impl.TestHelper;
import esthesis.util.redis.RedisUtils;
import esthesis.util.redis.RedisUtils.KeyType;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;

import static esthesis.core.common.AppConstants.REDIS_KEY_SUFFIX_TIMESTAMP;
import static esthesis.core.common.AppConstants.REDIS_KEY_SUFFIX_VALUE_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Slf4j
@QuarkusTest
class DTServiceTest {
	@Inject
	DTService dtService;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
	CommandSystemResource commandSystemResource;

	@InjectMock
	RedisUtils redisUtils;

	@Inject
	TestHelper testHelper;

	@BeforeEach
	void setUp() {

		// Mock redis calls to retrieve the measurements.
		when(redisUtils.getFromHash(
			KeyType.ESTHESIS_DM,
			"test-hardware-id",
			"test-category.test-measurement-1"))
			.thenReturn("test-value-1");

		when(redisUtils.getFromHash(
			KeyType.ESTHESIS_DM,
			"test-hardware-id",
			"test-category.test-measurement-2"))
			.thenReturn("test-value-2");

		when(redisUtils.getFromHash(
			KeyType.ESTHESIS_DM,
			"test-hardware-id",
			"test-category.test-measurement-1." + REDIS_KEY_SUFFIX_VALUE_TYPE))
			.thenReturn("test-value-type");

		when(redisUtils.getFromHash(
			KeyType.ESTHESIS_DM,
			"test-hardware-id",
			"test-category.test-measurement-2." + REDIS_KEY_SUFFIX_VALUE_TYPE))
			.thenReturn("test-value-type");

		when(redisUtils.getFromHash(
			KeyType.ESTHESIS_DM,
			"test-hardware-id",
			"test-category.test-measurement-1." + REDIS_KEY_SUFFIX_TIMESTAMP))
			.thenReturn(Instant.now().toString());

		when(redisUtils.getFromHash(
			KeyType.ESTHESIS_DM,
			"test-hardware-id",
			"test-category.test-measurement-2." + REDIS_KEY_SUFFIX_TIMESTAMP))
			.thenReturn(Instant.now().toString());

		when(redisUtils.getHash(
			KeyType.ESTHESIS_DM,
			"test-hardware-id"))
			.thenReturn(Map.of(
					"test-category.test-measurement-1", "test-value",
					"test-category.test-measurement-2", "test-value"
				)
			);

		// Mock command system resource count, getReplies and save.
		when(commandSystemResource.countCollectedReplies("test-correlation-id")).thenReturn(1L);
		when(commandSystemResource.getReplies("test-correlation-id")).thenReturn(testHelper.makeReplies(5));
		when(commandSystemResource.save(any(CommandRequestEntity.class))).thenReturn(testHelper.makeExecuteRequestScheduleInfo());


	}

	@Test
	void find() {
		// Assert valid parameters return a value.
		assertNotNull(dtService.find("test-hardware-id", "test-category", "test-measurement-1"));

		// Assert non-existing measurement does not return a value.
		assertNull(dtService.find("test-hardware-id", "test-category", "non-existing-measurement"));

		// Assert non-existing category does not return a value.
		assertNull(dtService.find("test-hardware-id", "non-existing-category", "test-measurement-1"));

		// Assert non-existing hardware id does not return a value.
		assertNull(dtService.find("non-existing-hardware-id", "test-category", "test-measurement-1"));
	}

	@Test
	void findAll() {

		// Assert a valid hardware id and category returns all measurements.
		assertFalse(dtService.findAll("test-hardware-id", "test-category").isEmpty());

		// Assert no replies are returned for non-existing hardware id.
		assertTrue(dtService.findAll("non-existing-hardware-id", "test-category").isEmpty());

		// Assert no replies are returned for non-existing category.
		assertTrue(dtService.findAll("test-hardware-id", "non-existing-category").isEmpty());

	}

	@Test
	void saveCommandRequest() {
		// Perform a save of a new command request.
		ExecuteRequestScheduleInfoDTO scheduleInfo =
			dtService.saveCommandRequest(
				new CommandRequestEntity()
					.setCommand("test-command")
					.setCommandType(CommandType.e)
					.setArguments("arg1,arg2")
					.setDescription("test-description")
					.setExecutionType(ExecutionType.a)
					.setHardwareIds("test-hardware-id")
			);

		// Assert command request was scheduled.
		assertNotNull(scheduleInfo);
		assertEquals(1, scheduleInfo.getDevicesScheduled());
		assertEquals(1, scheduleInfo.getDevicesMatched());
		assertNotNull(scheduleInfo.getCorrelationId());

	}

	@Test
	void getReplies() {
		// Assert replies are returned for a valid correlation id.
		assertFalse(dtService.getReplies("test-correlation-id").isEmpty());

		// Assert no replies are returned for a non-existing correlation id.
		assertTrue(dtService.getReplies("non-existing-correlation-id").isEmpty());
	}

	@Test
	void waitAndGetReplies() {
		// Prepare a valid schedule request info.
		ExecuteRequestScheduleInfoDTO scheduleInfo = new ExecuteRequestScheduleInfoDTO();
		scheduleInfo.setDevicesScheduled(1);
		scheduleInfo.setCorrelationId("test-correlation-id");
		scheduleInfo.setDevicesMatched(1);

		// Assert replies are returned.
		assertFalse(dtService.waitAndGetReplies(scheduleInfo).isEmpty());
	}
}
