package esthesis.service.command.impl.service;

import esthesis.common.avro.CommandType;
import esthesis.common.avro.ExecutionType;
import esthesis.service.command.dto.ExecuteRequestScheduleInfoDTO;
import esthesis.service.command.entity.CommandReplyEntity;
import esthesis.service.command.entity.CommandRequestEntity;
import esthesis.service.command.impl.TestHelper;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.resource.DeviceResource;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsResource;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.mockito.MockitoConfig;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Slf4j
@QuarkusTest
class CommandServiceTest {

	@Inject
	CommandService commandService;

	@Inject
	TestHelper testHelper;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
	SettingsResource settingsResource;

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
	DeviceResource deviceResource;

	int initialRequestsSizeInDB = 0;
	int initialRepliesSizeInDB = 0;

	@BeforeEach
	void setUp() {
		MockitoAnnotations.openMocks(this);
		testHelper.clearDatabase();
		testHelper.createMultipleCommandRequestEntities();
		testHelper.createMultipleCommandReplies();

		initialRequestsSizeInDB = testHelper.findAllCommandRequestEntities().size();
		log.info("Initial requests size in DB: {}", initialRequestsSizeInDB);

		initialRepliesSizeInDB = testHelper.findAllCommandReplyEntities().size();
		log.info("Initial replies size in DB: {}", initialRepliesSizeInDB);

		SettingEntity mockSettingEntity = mock(SettingEntity.class);
		when(mockSettingEntity.asString()).thenReturn("test setting");
		when(settingsResource.findByName(any())).thenReturn(mockSettingEntity);
	}

	@Test
	void findDevicesByHardwareId() {
		when(deviceResource.findByHardwareIds(anyString(), anyBoolean())).thenReturn(List.of(mock(DeviceEntity.class)));

		assertDoesNotThrow(() -> commandService.findDevicesByHardwareId("test-hardware-id"));

		verify(deviceResource, times(1)).findByHardwareIds(any(), eq(true));

		verify(settingsResource, times(1)).findByName(any());
	}

	@Test
	void saveRequest() {
		CommandRequestEntity newCommandRequest =
			testHelper.makeCommandRequestEntity(
				"test-hardware-id",
				null,
				CommandType.e,
				ExecutionType.a);

		commandService.saveRequest(newCommandRequest);

		assertEquals(initialRequestsSizeInDB + 1, testHelper.findAllCommandRequestEntities().size());
	}

	@Test
	void saveRequestAndExecute() {
		CommandRequestEntity newCommandRequest =
			testHelper.makeCommandRequestEntity(
				"test-hardware-id",
				null,
				CommandType.e,
				ExecutionType.a);

		ExecuteRequestScheduleInfoDTO scheduleInfo = commandService.saveRequestAndExecute(newCommandRequest);

		assertEquals(1, scheduleInfo.getDevicesMatched());
		assertEquals(1, scheduleInfo.getDevicesScheduled());
		assertNotNull(scheduleInfo.getCorrelationId());
		assertEquals(initialRequestsSizeInDB + 1, testHelper.findAllCommandRequestEntities().size());
	}

	@Test
	void executeRequest() {
		CommandRequestEntity newCommandRequest =
			testHelper.makeCommandRequestEntity(
					"test-hardware-id",
					null,
					CommandType.e,
					ExecutionType.a)
				.setDispatchedOn(null);

		testHelper.createCommandRequestEntity(newCommandRequest);

		ExecuteRequestScheduleInfoDTO scheduleInfo =
			commandService.executeRequest(newCommandRequest.getId().toString());

		assertEquals(1, scheduleInfo.getDevicesMatched());
		assertEquals(1, scheduleInfo.getDevicesScheduled());

	}

	@Test
	void findCommandRequest() {
		List<CommandRequestEntity> requestEntities =
			commandService.findCommandRequest(
				testHelper.makePageable(0, 100), true)
				.getContent();

		assertEquals(initialRequestsSizeInDB, requestEntities.size());
	}

	@Test
	void getCommand() {
		String commandId = testHelper.findOneCommandRequestEntity().getId().toString();

		CommandRequestEntity commandRequest = commandService.getCommand(commandId);

		assertNotNull(commandRequest);
	}

	@Test
	void getReplies() {

		assertEquals(0, commandService.getReplies(new ObjectId().toString()).size()); // Non-existent Correlation ID

		String correlationId = testHelper.findOneCommandReplyEntity().getCorrelationId();

		List<CommandReplyEntity> replies = commandService.getReplies(correlationId);

		// There should be two replies for each correlation ID as created in setUp
		assertEquals(2, replies.size());
	}

	@Test
	void deleteCommand() {
		String commandId = testHelper.findOneCommandRequestEntity().getId().toString();

		commandService.deleteCommand(commandId);

		assertEquals(initialRequestsSizeInDB - 1, testHelper.findAllCommandRequestEntities().size());
	}

	@Test
	void deleteReply() {

		commandService.deleteReply(new ObjectId().toString()); // Non-existent ID
		assertEquals(initialRepliesSizeInDB, testHelper.findAllCommandReplyEntities().size());

		String replyId = testHelper.findOneCommandReplyEntity().getId().toString();
		commandService.deleteReply(replyId);
		assertEquals(initialRepliesSizeInDB - 1, testHelper.findAllCommandReplyEntities().size());
	}

	@Test
	void purge() {
		commandService.purge(10);
		assertEquals(initialRequestsSizeInDB, testHelper.findAllCommandRequestEntities().size());
		assertEquals(initialRepliesSizeInDB, testHelper.findAllCommandReplyEntities().size());

		commandService.purge(0);
		assertEquals(0, testHelper.findAllCommandRequestEntities().size());
		assertEquals(0, testHelper.findAllCommandReplyEntities().size());
	}

	@Test
	void countCollectedReplies() {
		assertEquals(0, commandService.countCollectedReplies(new ObjectId().toString())); // Non-existent ID

		String correlationId = testHelper.findOneCommandReplyEntity().getCorrelationId();

		// There should be two replies for each correlation ID as created in setUp
		assertEquals(2, commandService.countCollectedReplies(correlationId));
	}

	@Test
	void deleteReplies() {
		commandService.deleteReplies(new ObjectId().toString()); // Non-existent  correlation ID
		assertEquals(initialRepliesSizeInDB, testHelper.findAllCommandReplyEntities().size());

		String correlationId = testHelper.findOneCommandReplyEntity().getCorrelationId();

		commandService.deleteReplies(correlationId);

		// There should be two replies for each correlation ID as created in setUp
		assertEquals(initialRepliesSizeInDB - 2, testHelper.findAllCommandReplyEntities().size());
	}

	@Test
	void replayCommand() {

		String commandId = testHelper.findOneCommandRequestEntity().getId().toString();
		commandService.replayCommand(commandId);

		assertEquals(initialRequestsSizeInDB + 1, testHelper.findAllCommandRequestEntities().size());
	}

}
