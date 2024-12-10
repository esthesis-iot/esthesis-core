package esthesis.service.command.impl.service;

import esthesis.service.command.impl.TestHelper;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class CommandReplyServiceTest {

	@Inject
	CommandReplyService commandReplyService;

	@Inject
	TestHelper testHelper;

	@BeforeEach
	void setUp() {
		testHelper.clearDatabase();

		// Persist 6 command reply entities for 2 unique hardware IDs:
		// 3 entities with success execution and 3 with failed execution
		// 4 entities with the current timestamp and 2 entities with a timestamp from 2 days ago.
		testHelper.createCommandReplyEntity(
			testHelper.makeSuccessCommandReplyEntity(
				"hardware-test-1",
				"correlationId-1"
			)
		);
		testHelper.createCommandReplyEntity(
			testHelper.makeSuccessCommandReplyEntity(
				"hardware-test-2",
				"correlationId-2"
			)
		);
		testHelper.createCommandReplyEntity(
			testHelper.makeFailedCommandReplyEntity(
				"hardware-test-1",
				"correlationId-3"
			)
		);
		testHelper.createCommandReplyEntity(
			testHelper.makeFailedCommandReplyEntity(
				"hardware-test-2",
				"correlationId-4"
			)
		);

		testHelper.createCommandReplyEntity(
			testHelper.makeSuccessCommandReplyEntity(
				"hardware-test-1",
				"correlationId-5"
			).setCreatedOn(
				Instant.now().minus(2, ChronoUnit.DAYS))
		);
		testHelper.createCommandReplyEntity(
			testHelper.makeFailedCommandReplyEntity(
				"hardware-test-1",
				"correlationId-6"
			).setCreatedOn(
				Instant.now().minus(2, ChronoUnit.DAYS))
		);
	}

	@Test
	void findByCorrelationId() {
		assertEquals(1, commandReplyService.findByCorrelationId("correlationId-1").size());
		assertEquals(0, commandReplyService.findByCorrelationId("correlationId-unexistent").size());
	}

	@Test
	void purge() {
		commandReplyService.purge(7);
		assertEquals(6,  testHelper.findAllCommandReplyEntities().size());

		commandReplyService.purge(2);
		assertEquals(4,  testHelper.findAllCommandReplyEntities().size());

		commandReplyService.purge(1);
		assertEquals(4,  testHelper.findAllCommandReplyEntities().size());

		commandReplyService.purge(0);
		assertEquals(0,  testHelper.findAllCommandReplyEntities().size());
	}

	@Test
	void deleteById() {
		String id = testHelper.findOneCommandReplyEntity().getId().toString();
		commandReplyService.deleteById(id);
		assertEquals(5,  testHelper.findAllCommandReplyEntities().size());
	}

	@Test
	void countByColumn() {
		assertEquals(2,  commandReplyService.countByColumn("hardwareId", "hardware-test-2"));
		assertEquals(0,  commandReplyService.countByColumn("hardwareId", "hardware-test-unexistent"));
		assertEquals(1,  commandReplyService.countByColumn("correlationId", "correlationId-1"));
	}

	@Test
	void deleteByColumn() {
		commandReplyService.deleteByColumn("hardwareId", "hardware-test-unexistent");
		assertEquals(6,  testHelper.findAllCommandReplyEntities().size());

		commandReplyService.deleteByColumn("hardwareId", "hardware-test-2");
		assertEquals(4,  testHelper.findAllCommandReplyEntities().size());
	}
}
