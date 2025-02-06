package esthesis.service.command.impl.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import esthesis.common.avro.CommandType;
import esthesis.common.avro.ExecutionType;
import esthesis.service.command.entity.CommandRequestEntity;
import esthesis.service.command.impl.TestHelper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@Slf4j
@QuarkusTest
class CommandRequestServiceTest {

	@Inject
	CommandRequestService commandRequestService;

	@Inject
	TestHelper testHelper;

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();
	}

	@Test
	void purge() {
		// Perform a save operation for a new request with creation date as now.
		String commandId = commandRequestService.save(
				testHelper.makeCommandRequestEntity(
						"test-hardware-id",
						"test-tag",
						CommandType.e,
						ExecutionType.s)
					.setCreatedOn(Instant.now()))
			.getId()
			.toHexString();

		// Assert command request exists
		assertNotNull(commandRequestService.findById(commandId));

		// Perform a purge operation for 1 day or more.
		commandRequestService.purge(1);

		// Assert command request still exists
		assertNotNull(commandRequestService.findById(commandId));

		// Perform a purge operation for 0 days or more.
		commandRequestService.purge(0);

		// Assert command request no longer exists
		assertNull(commandRequestService.findById(commandId));

	}

	@Test
	void save() {
		// Perform a save operation for a new request.
		String commandId =
			commandRequestService.save(
					new CommandRequestEntity(
						"test-hardware-id",
						"tag-test",
						CommandType.e,
						ExecutionType.s,
						"test command",
						"test args",
						"test description",
						Instant.now(),
						Instant.now())
				).getId()
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
	void findById() {
		// Perform a save operation for a new request
		String commandId = commandRequestService.save(
				testHelper.makeCommandRequestEntity(
					"test-hardware-id",
					"test-tag",
					CommandType.e,
					ExecutionType.s))
			.getId()
			.toHexString();

		// Assert command request can be found.
		assertNotNull(commandRequestService.findById(commandId));
	}

	@Test
	void find() {
		// Assert that no command requests exist.
		assertTrue(
			commandRequestService.find(
					testHelper.makePageable(1, 10))
				.getContent()
				.isEmpty());

		// Perform a save operation for a new request.
		commandRequestService.save(
			testHelper.makeCommandRequestEntity(
				"test-hardware-id",
				"test-tag",
				CommandType.e,
				ExecutionType.s));

		// Assert that a command requests exists.
		assertFalse(commandRequestService.find(
				testHelper.makePageable(0, 10))
			.getContent()
			.isEmpty());
	}

	@Test
	void deleteById() {
		// Perform a save operation for a new request
		String commandId = commandRequestService.save(
				testHelper.makeCommandRequestEntity(
					"test-hardware-id",
					"test-tag",
					CommandType.e,
					ExecutionType.s))
			.getId()
			.toHexString();

		// Assert command request can be found.
		assertNotNull(commandRequestService.findById(commandId));

		// Delete command request.
		commandRequestService.deleteById(commandId);

		// Assert command request can no longer be found.
		assertNull(commandRequestService.findById(commandId));
	}
}
