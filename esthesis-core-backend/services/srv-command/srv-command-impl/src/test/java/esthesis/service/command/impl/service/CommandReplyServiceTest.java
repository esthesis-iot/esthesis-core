package esthesis.service.command.impl.service;

import esthesis.service.command.impl.TestHelper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class CommandReplyServiceTest {

	@Inject
	CommandReplyService commandReplyService;

	@Inject
	TestHelper testHelper;

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();
	}

	@Test
	void findByCorrelationId() {
		// Perform a save operation for a new reply.
		testHelper.createCommandReplyEntity("hardware-test-1",
			"correlationId-1",
			"test-output",
			true);

		// Assert that there is reply for the correlation ID.
		assertFalse(commandReplyService.findByCorrelationId("correlationId-1").isEmpty());

		// Assert that there are no replies for non-existent correlation ID.
		assertTrue(commandReplyService.findByCorrelationId("correlationId-non-existent").isEmpty());
	}

	@Test
	void purge() {
		// Perform a save operation for a new reply with creation date as now.
		testHelper.createCommandReplyEntity("hardware-test-1",
			"correlationId-1",
			"test-output",
			true);

		// Perform a purge operation for 1 day or more
		commandReplyService.purge(1);

		// Assert the reply was not deleted.
		assertFalse(commandReplyService.findByCorrelationId("correlationId-1").isEmpty());

		// Perform a purge operation for 0 days or more.
		commandReplyService.purge(0);


		// Assert the reply was deleted.
		assertTrue(commandReplyService.findByCorrelationId("correlationId-1").isEmpty());

	}

	@Test
	void deleteById() {
		// Perform a save operation for a new reply.
		String replyId =
			testHelper.createCommandReplyEntity("hardware-test-1",
					"correlationId-1",
					"test-output",
					true)
				.getId()
				.toHexString();

		// Assert that there is reply for the correlation ID.
		assertFalse(commandReplyService.findByCorrelationId("correlationId-1").isEmpty());

		// Perform a delete operation for the reply ID.
		commandReplyService.deleteById(replyId);

		// Assert that there is no reply for the correlation ID.
		assertTrue(commandReplyService.findByCorrelationId("correlationId-1").isEmpty());

	}

	@Test
	void countByColumn() {
		// Perform a save operation for a new reply.
		testHelper.createCommandReplyEntity("hardware-test-1",
			"correlationId-1",
			"test-output",
			true);

		// Assert correct values counted.
		assertEquals(1, commandReplyService.countByColumn("hardwareId", "hardware-test-1"));
		assertEquals(1, commandReplyService.countByColumn("success", true));

		// Assert wrong values counted.
		assertEquals(0, commandReplyService.countByColumn("hardwareId", "hardware-test-unexistent"));
		assertEquals(0, commandReplyService.countByColumn("success", false));
	}

	@Test
	void deleteByColumn() {
		// Perform a save operation for a new reply.
		testHelper.createCommandReplyEntity("hardware-test-1",
			"correlationId-1",
			"test-output",
			true);

		// Perform a delete operation for an non-existent hardware ID.
		commandReplyService.deleteByColumn("hardwareId", "hardware-test-non-existent");

		// Assert that there is reply for the correlation ID.
		assertFalse(commandReplyService.findByCorrelationId("correlationId-1").isEmpty());


		// Perform a delete operation for the correct hardware ID.
		commandReplyService.deleteByColumn("hardwareId", "hardware-test-1");

		// Assert that there is no reply for the correlation ID.
		assertTrue(commandReplyService.findByCorrelationId("correlationId-1").isEmpty());


	}
}
