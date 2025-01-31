package esthesis.service.command.impl.service;

import esthesis.common.avro.CommandType;
import esthesis.common.avro.ExecutionType;
import esthesis.service.command.dto.ExecuteRequestScheduleInfoDTO;
import esthesis.service.command.entity.CommandRequestEntity;
import esthesis.service.command.impl.TestHelper;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.device.resource.DeviceResource;
import esthesis.service.settings.entity.SettingEntity;
import esthesis.service.settings.resource.SettingsResource;
import esthesis.service.tag.resource.TagResource;
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

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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

	@InjectMock
	@MockitoConfig(convertScopes = true)
	@RestClient
	TagResource tagResource;

	@Inject
	CommandRequestService commandRequestService;


	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();

		MockitoAnnotations.openMocks(this);

		// Mock settings and tag resources.
		SettingEntity mockSettingEntity = mock(SettingEntity.class);
		when(mockSettingEntity.asString()).thenReturn("test setting");
		when(settingsResource.findByName(any())).thenReturn(mockSettingEntity);
		when(tagResource.findByNames(anyString(), anyBoolean())).thenReturn(List.of());
	}

	@Test
	void findDevicesByHardwareId() {
		// Mock finding devices by hardware ID.
		when(deviceResource.findByHardwareIds(anyString(), anyBoolean())).thenReturn(List.of(mock(DeviceEntity.class)));

		// Assert no exception is thrown while finding devices by hardware ID.
		assertDoesNotThrow(() -> commandService.findDevicesByHardwareId("test-hardware-id"));

		// Verify that the device and settings resources were called.
		verify(deviceResource, times(1)).findByHardwareIds(any(), eq(true));
		verify(settingsResource, times(1)).findByName(any());
	}

	@Test
	void saveRequest() {
		// Perform a save operation for a new request.
		String commandId =
			commandService.saveRequest(
					new CommandRequestEntity(
						"test-hardware-id",
						"tag-test",
						CommandType.e,
						ExecutionType.s,
						"test command",
						"test args",
						"test description",
						Instant.now(),
						Instant.now()))
				.toHexString();

		// Assert command request was persisted with the expected values.
		CommandRequestEntity commandRequest = commandRequestService.findById(commandId);
		assertEquals("test-hardware-id", commandRequest.getHardwareIds());
		assertEquals("tag-test", commandRequest.getTags());
		assertEquals(CommandType.e, commandRequest.getCommandType());
		assertEquals(ExecutionType.s, commandRequest.getExecutionType());
		assertEquals("test command", commandRequest.getCommand());
		assertEquals("test args", commandRequest.getArguments());
		assertEquals("test description", commandRequest.getDescription());
		assertNotNull(commandRequest.getCreatedOn());
		assertNotNull(commandRequest.getDispatchedOn());


	}

	@Test
	void saveRequestAndExecute() {
		// Perform a save and execution operation for a new request.
		ExecuteRequestScheduleInfoDTO scheduleInfo =
			commandService.saveRequestAndExecute(
				testHelper.makeCommandRequestEntity(
					"test-hardware-id",
					null,
					CommandType.e,
					ExecutionType.a)
			);

		// Assert devices were matched and command request was persisted.
		assertEquals(1, scheduleInfo.getDevicesMatched());
		assertEquals(1, scheduleInfo.getDevicesScheduled());
		assertNotNull(commandRequestService.findById(scheduleInfo.getCorrelationId()));

	}

	@Test
	void executeRequest() {
		// Perform a save operation for a new request.
		String commandId =
			commandService.saveRequest(
					testHelper.makeCommandRequestEntity(
						"test-hardware-id",
						"test-tag",
						CommandType.e,
						ExecutionType.a))
				.toHexString();

		// Perform an execution operation.
		ExecuteRequestScheduleInfoDTO scheduleInfo = commandService.executeRequest(commandId);


		// Assert devices were matched and command request was persisted.
		assertEquals(1, scheduleInfo.getDevicesMatched());
		assertEquals(1, scheduleInfo.getDevicesScheduled());
		assertNotNull(commandRequestService.findById(scheduleInfo.getCorrelationId()));

	}

	@Test
	void findCommandRequest() {
		// Assert no command requests exist.
		assertTrue(commandService.findCommandRequest(
				testHelper.makePageable(0, 100),
				true)
			.getContent()
			.isEmpty()
		);


		// Perform a save operation for a new request.
		String commandId =
			commandService.saveRequest(
					testHelper.makeCommandRequestEntity(
						"test-hardware-id",
						"test-tag",
						CommandType.e,
						ExecutionType.a))
				.toHexString();


		// Assert command request can be found.
		assertFalse(commandService.findCommandRequest(
				testHelper.makePageable(0, 100),
				true)
			.getContent()
			.isEmpty()
		);
	}

	@Test
	void getCommand() {
		// Perform a save operation for a new request.
		String commandId =
			commandService.saveRequest(
					testHelper.makeCommandRequestEntity(
						"test-hardware-id",
						"test-tag",
						CommandType.e,
						ExecutionType.a))
				.toHexString();


		// Assert command request can be found.
		assertNotNull(commandService.getCommand(commandId));

		// Assert non-existent command request cannot be found.
		assertNull(commandService.getCommand(new ObjectId().toHexString()));
	}

	@Test
	void getReplies() {
		// Perform a save operation for a new request.
		String commandId =
			commandService.saveRequest(
					testHelper.makeCommandRequestEntity(
						"test-hardware-id",
						"test-tag",
						CommandType.e,
						ExecutionType.a))
				.toHexString();


		// Assert replies cannot be found for the given command ID.
		assertTrue(commandService.getReplies(commandId).isEmpty());

		// Perform a save operation for a new reply using the command ID.
		testHelper.createCommandReplyEntity("test-hardware-id", commandId, "test-output", true);

		// Assert replies can be found for the given command ID.
		assertFalse(commandService.getReplies(commandId).isEmpty());
	}

	@Test
	void deleteCommand() {
		// Perform a save operation for a new request.
		String commandId =
			commandService.saveRequest(
					testHelper.makeCommandRequestEntity(
						"test-hardware-id",
						"test-tag",
						CommandType.e,
						ExecutionType.a))
				.toHexString();

		// Assert command request can be found.
		assertNotNull(commandRequestService.findById(commandId));

		// Perform a delete operation for the given command ID.
		commandService.deleteCommand(commandId);

		// Assert command request cannot be found.
		assertNull(commandRequestService.findById(commandId));

	}

	@Test
	void deleteReply() {
		// Perform a save operation for a new request.
		String commandId =
			commandService.saveRequest(
					testHelper.makeCommandRequestEntity(
						"test-hardware-id",
						"test-tag",
						CommandType.e,
						ExecutionType.a))
				.toHexString();

		// Perform a save operation for a new reply using the command ID.
		String replyId =
			testHelper.createCommandReplyEntity(
					"test-hardware-id",
					commandId,
					"test-output",
					true)
				.getId()
				.toHexString();

		// Assert replies can be found for the given command ID.
		assertFalse(commandService.getReplies(commandId).isEmpty());

		// Perform a delete operation for the given reply ID.
		commandService.deleteReply(replyId);

		// Assert replies cannot be found for the given command ID.
		assertTrue(commandService.getReplies(commandId).isEmpty());
	}

	@Test
	void purge() {
		// Perform a save operation for a new request.
		String commandId =
			commandService.saveRequest(
					testHelper.makeCommandRequestEntity(
						"test-hardware-id",
						"test-tag",
						CommandType.e,
						ExecutionType.a))
				.toHexString();

		// Perform a save operation for a new reply using the command ID.
		testHelper.createCommandReplyEntity("test-hardware-id", commandId, "test-output", true);

		// Assert reply and command request can be found.
		assertFalse(commandService.getReplies(commandId).isEmpty());
		assertNotNull(commandRequestService.findById(commandId));

		// Perform a purge operation for 0 day or more.
		commandService.purge(0);

		// Assert reply and command were deleted.
		assertTrue(commandService.getReplies(commandId).isEmpty());
		assertNull(commandRequestService.findById(commandId));
	}

	@Test
	void countCollectedReplies() {
		// Perform a save operation for a new request.
		String commandId =
			commandService.saveRequest(
					testHelper.makeCommandRequestEntity(
						"test-hardware-id",
						"test-tag",
						CommandType.e,
						ExecutionType.a))
				.toHexString();

		// Assert no replies for the given command ID.
		assertEquals(0, commandService.countCollectedReplies(commandId));

		// Perform a save operation for a new reply using the command ID.
		testHelper.createCommandReplyEntity("test-hardware-id", commandId, "test-output", true);

		// Assert can count replies for the given command ID.
		assertEquals(1, commandService.countCollectedReplies(commandId));

	}

	@Test
	void deleteReplies() {
		// Perform a save operation for a new request.
		String commandId =
			commandService.saveRequest(
					testHelper.makeCommandRequestEntity(
						"test-hardware-id",
						"test-tag",
						CommandType.e,
						ExecutionType.a))
				.toHexString();

		// Perform a save operation for a new reply using the command ID.
		testHelper.createCommandReplyEntity("test-hardware-id", commandId, "test-output", true);

		// Assert replies can be found for the given command ID.
		assertFalse(commandService.getReplies(commandId).isEmpty());

		// Perform a delete operation for the given command ID.
		commandService.deleteReplies(commandId);

		// Assert replies cannot be found for the given command ID.
		assertTrue(commandService.getReplies(commandId).isEmpty());
	}

	@Test
	void replayCommand() {

		// Perform a save operation for a new request.
		String commandId =
			commandService.saveRequest(
					testHelper.makeCommandRequestEntity(
						"test-hardware-id",
						"test-tag",
						CommandType.e,
						ExecutionType.a))
				.toHexString();

		// Assert there is one command request persisted.
		assertEquals(
			1,
			commandService.findCommandRequest(
					testHelper.makePageable(0, 100),
					true)
				.getContent()
				.size());

		// Perform a replay operation for the given command ID.
		commandService.replayCommand(commandId);

		// Assert there are two command requests persisted.
		assertEquals(
			2,
			commandService.findCommandRequest(
					testHelper.makePageable(0, 100),
					true)
				.getContent()
				.size());
	}

}
