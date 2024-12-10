package esthesis.service.command.impl;

import esthesis.common.avro.CommandType;
import esthesis.common.avro.ExecutionType;
import esthesis.core.common.entity.BaseEntity;
import esthesis.service.command.entity.CommandReplyEntity;
import esthesis.service.command.entity.CommandRequestEntity;
import esthesis.service.command.impl.repository.CommandReplyRepository;
import esthesis.service.command.impl.repository.CommandRequestRepository;
import esthesis.service.common.paging.Pageable;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.UriInfo;
import org.apache.commons.lang3.StringUtils;
import org.instancio.Instancio;
import org.mockito.Mockito;

import java.net.URI;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.instancio.Select.all;
import static org.instancio.Select.field;
import static org.mockito.Mockito.when;

@ApplicationScoped
public class TestHelper {

	@Inject
	CommandReplyRepository commandReplyRepository;

	@Inject
	CommandRequestRepository commandRequestRepository;


	public void clearDatabase() {
		commandRequestRepository.deleteAll();
		commandReplyRepository.deleteAll();
	}


	public void createCommandReplyEntity(CommandReplyEntity commandReplyEntity) {
		commandReplyRepository.persist(commandReplyEntity);
	}

	public CommandReplyEntity makeSuccessCommandReplyEntity(String hardwareId, String correlationId) {
		return new CommandReplyEntity()
			.setCorrelationId(correlationId)
			.setHardwareId(hardwareId)
			.setCreatedOn(Instant.now())
			.setOutput("success-output")
			.setSuccess(true)
			.setTrimmed(false);
	}

	public CommandReplyEntity makeFailedCommandReplyEntity(String hardwareId, String correlationId) {
		return new CommandReplyEntity()
			.setCorrelationId(correlationId)
			.setHardwareId(hardwareId)
			.setCreatedOn(Instant.now())
			.setOutput("failed-output")
			.setSuccess(false)
			.setTrimmed(false);
	}

	public Collection<CommandReplyEntity> findAllCommandReplyEntities() {
		return commandReplyRepository.findAll().list();
	}

	public CommandReplyEntity findOneCommandReplyEntity() {
		return commandReplyRepository.findAll().firstResult();
	}

	public void createCommandRequestEntity(CommandRequestEntity commandRequestEntity) {
		commandRequestRepository.persist(commandRequestEntity);
	}

	public CommandRequestEntity makeCommandRequestEntity(String hardwareIds,
																											 String tags,
																											 CommandType commandType,
																											 ExecutionType executionType) {
		return Instancio.of(CommandRequestEntity.class)
			.ignore(all(field(BaseEntity.class, "id")))
			.set(all(field("hardwareIds")), hardwareIds)
			.set(all(field("tags")), tags)
			.set(all(field("commandType")), commandType)
			.set(all(field("executionType")), executionType)
			.set(all(field("createdOn")), Instant.now())
			.set(all(field("dispatchedOn")), Instant.now())
			.create();

	}

	/**
	 * Creates multiple CommandRequestEntity objects with various combinations of hardware IDs,
	 * tags, command types, and execution types. The entities are set with different creation
	 * timestamps to simulate entities created 1, 3, and 5 days ago. Each entity is then persisted
	 */
	public void createMultipleCommandRequestEntities() {
		List<String> hardwareIds = List.of("hardware-test-1", "hardware-test-2");
		List<String> tags = List.of("tag-test-1", "tag-test-2");
		List<CommandType> commandTypes = List.of(CommandType.h, CommandType.f, CommandType.p, CommandType.r, CommandType.e);
		List<ExecutionType> executionTypes = List.of(ExecutionType.a, ExecutionType.s);

		List<CommandRequestEntity> commandRequestEntities = new ArrayList<>();
		// Create entities with all possible combinations
		for (CommandType commandType : commandTypes) {
			for (ExecutionType executionType : executionTypes) {
				for (String hardwareId : hardwareIds) {
					commandRequestEntities.add(makeCommandRequestEntity(hardwareId, null, commandType, executionType));
				}

				commandRequestEntities.add(
					makeCommandRequestEntity(String.join(",", hardwareIds), null, commandType, executionType)
				);

				for (String tag : tags) {
					commandRequestEntities.add(makeCommandRequestEntity(null, tag, commandType, executionType));
				}
			}
		}

		// set 3 entities as created 5 days ago
		for (int i = 0; i < 3; i++) {
			commandRequestEntities.get(i).setCreatedOn(Instant.now().minus(5, ChronoUnit.DAYS));
		}

		// set 3 entities as created 3 days ago
		for (int i = 3; i < 6; i++) {
			commandRequestEntities.get(i).setCreatedOn(Instant.now().minus(3, ChronoUnit.DAYS));
		}

		// set 3 entities as created 1 day ago
		for (int i = 6; i < 9; i++) {
			commandRequestEntities.get(i).setCreatedOn(Instant.now().minus(1, ChronoUnit.DAYS));
		}

		// Persist it all
		commandRequestEntities.forEach(commandRequestEntity -> commandRequestRepository.persist(commandRequestEntity));

	}

	/**
	 * Creates a success and a failed reply for each command request.
	 */
	public void createMultipleCommandReplies() {
		// Creates a success reply for each command request
		findAllCommandRequestEntities()
			.stream()
			.filter(commandRequestEntity -> StringUtils.isNotBlank(commandRequestEntity.getHardwareIds()))
			.forEach(commandRequestEntity -> Arrays.stream(
				commandRequestEntity.getHardwareIds().split(",")).toList().forEach(
				hardwareId -> createCommandReplyEntity(
					makeSuccessCommandReplyEntity(hardwareId, commandRequestEntity.getId().toString())
				)));

		// Creates a failed reply for each command request
		findAllCommandRequestEntities()
			.stream()
			.filter(commandRequestEntity -> StringUtils.isNotBlank(commandRequestEntity.getHardwareIds()))
			.forEach(commandRequestEntity -> Arrays.stream(
			commandRequestEntity.getHardwareIds().split(",")).toList().forEach(
			hardwareId -> createCommandReplyEntity(
				makeFailedCommandReplyEntity(hardwareId, commandRequestEntity.getId().toString())
			)));
	}

	public Collection<CommandRequestEntity> findAllCommandRequestEntities() {
		return commandRequestRepository.findAll().list();
	}

	public CommandRequestEntity findOneCommandRequestEntity() {
		return commandRequestRepository.findAll().firstResult();
	}

	/**
	 * Helper method to create a Pageable object with the specified parameters
	 */
	public Pageable makePageable(int page, int size) {

		// Create a mock of UriInfo
		UriInfo uriInfo = Mockito.mock(UriInfo.class);

		// Define the behavior of the mock
		when(uriInfo.getRequestUri()).thenReturn(URI.create("http://localhost:8080/find?page=" + page + "&size=" + size));
		when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());

		Pageable pageable = new Pageable();
		pageable.setPage(page);
		pageable.setSize(size);
		pageable.setSort("");
		pageable.setUriInfo(uriInfo);
		return pageable;
	}
}
