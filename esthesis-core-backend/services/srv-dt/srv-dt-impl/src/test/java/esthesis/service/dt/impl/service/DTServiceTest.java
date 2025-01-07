package esthesis.service.dt.impl.service;

import esthesis.common.avro.CommandType;
import esthesis.common.avro.ExecutionType;
import esthesis.service.command.dto.ExecuteRequestScheduleInfoDTO;
import esthesis.service.command.entity.CommandRequestEntity;
import esthesis.service.command.resource.CommandSystemResource;
import esthesis.service.dt.dto.DTValueReplyDTO;
import esthesis.service.dt.impl.TestHelper;
import esthesis.util.redis.RedisUtils;
import esthesis.util.redis.RedisUtils.KeyType;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static esthesis.core.common.AppConstants.REDIS_KEY_SUFFIX_TIMESTAMP;
import static esthesis.core.common.AppConstants.REDIS_KEY_SUFFIX_VALUE_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

		// Mock relevant redis calls
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

		// Mock relevant command request calls

		when(commandSystemResource.countCollectedReplies(anyString())).thenReturn(1L);
		when(commandSystemResource.getReplies(anyString())).thenReturn(testHelper.makeReplies(5));
		when(commandSystemResource.save(any(CommandRequestEntity.class))).thenReturn(testHelper.makeExecuteRequestScheduleInfo());


	}

	@Test
	void find() {
		// Act
		DTValueReplyDTO replyDTO =
			dtService.find("test-hardware-id", "test-category", "test-measurement-1");

		// Assert
		assertNotNull(replyDTO);
		assertEquals("test-value-1", replyDTO.getValue());
	}

	@Test
	void findAll() {
		// Act
		List<DTValueReplyDTO> replies = dtService.findAll("test-hardware-id", "test-category");

		// Assert
		assertEquals(2, replies.size());

	}

	@Test
	void saveCommandRequest() {
		// Arrange
		CommandRequestEntity newCommand = new CommandRequestEntity();
		newCommand.setCommand("test-command");
		newCommand.setCommandType(CommandType.e);
		newCommand.setArguments("arg1,arg2");
		newCommand.setDescription("test-description");
		newCommand.setExecutionType(ExecutionType.a);
		newCommand.setHardwareIds("test-hardware-id");

		// Act
		ExecuteRequestScheduleInfoDTO scheduleInfo = dtService.saveCommandRequest(newCommand);

		// Assert
		assertNotNull(scheduleInfo);
		assertEquals(1, scheduleInfo.getDevicesScheduled());
		assertEquals(1, scheduleInfo.getDevicesMatched());
		assertNotNull(scheduleInfo.getCorrelationId());

	}

	@Test
	void getReplies() {
		// Act
		List<Document> replies = dtService.getReplies("test-correlation-id");

		// Assert
		assertFalse(replies.isEmpty());
	}

	@Test
	void waitAndGetReplies() {
		// Arrange
		ExecuteRequestScheduleInfoDTO scheduleInfo = new ExecuteRequestScheduleInfoDTO();
		scheduleInfo.setDevicesScheduled(1);
		scheduleInfo.setCorrelationId("test-correlation-id");
		scheduleInfo.setDevicesMatched(1);

		// Act
		List<Document> replies = dtService.waitAndGetReplies(scheduleInfo);

		// Assert
		assertFalse(replies.isEmpty());
	}
}
