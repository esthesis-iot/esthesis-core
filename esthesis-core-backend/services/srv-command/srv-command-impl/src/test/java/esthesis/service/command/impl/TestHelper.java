package esthesis.service.command.impl;

import esthesis.common.avro.CommandType;
import esthesis.common.avro.ExecutionType;
import esthesis.common.util.EsthesisCommonConstants;
import esthesis.core.common.AppConstants;
import esthesis.core.common.entity.BaseEntity;
import esthesis.core.common.entity.CommandReplyEntity;
import esthesis.service.command.entity.CommandRequestEntity;
import esthesis.service.command.impl.repository.CommandReplyRepository;
import esthesis.service.command.impl.repository.CommandRequestRepository;
import esthesis.service.common.paging.Pageable;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.tag.entity.TagEntity;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.UriInfo;
import org.bson.types.ObjectId;
import org.instancio.Instancio;
import org.mockito.Mockito;

import java.net.URI;
import java.time.Instant;

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


	public CommandReplyEntity createCommandReplyEntity(String hardwareId,
																										 String correlationId,
																										 String output,
																										 boolean success) {
		CommandReplyEntity commandReplyEntity = new CommandReplyEntity()
			.setCorrelationId(correlationId)
			.setHardwareId(hardwareId)
			.setCreatedOn(Instant.now())
			.setOutput(output)
			.setSuccess(success)
			.setTrimmed(false);

		commandReplyRepository.persist(commandReplyEntity);
		return commandReplyEntity;
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
	 * Mock a Pageable object with the specified parameters.
	 *
	 * @param page The page number being requested.
	 * @param size The size of the page.
	 * @return The mocked Pageable object.
	 */
	public Pageable makePageable(int page, int size) {

		// Mock the request URI and parameters.
		UriInfo uriInfo = Mockito.mock(UriInfo.class);
		when(uriInfo.getRequestUri()).thenReturn(URI.create("http://localhost:8080/find?page=" + page + "&size=" + size));
		when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());

		Pageable pageable = new Pageable();
		pageable.setPage(page);
		pageable.setSize(size);
		pageable.setSort("");
		pageable.setUriInfo(uriInfo);
		return pageable;
	}

	public TagEntity mockTagEntity(String name) {
		TagEntity tagEntity = new TagEntity();
		tagEntity.setName(name);
		tagEntity.setDescription("Test tag");
		tagEntity.setId(new ObjectId());
		return tagEntity;
	}

    public DeviceEntity mockDeviceEntity(String hardwareId) {
				DeviceEntity deviceEntity = new DeviceEntity();
				deviceEntity.setHardwareId(hardwareId);
				deviceEntity.setType(EsthesisCommonConstants.Device.Type.CORE);
				deviceEntity.setCreatedOn(Instant.now());
				deviceEntity.setRegisteredOn(Instant.now());
				deviceEntity.setStatus(AppConstants.Device.Status.REGISTERED);
				deviceEntity.setId(new ObjectId());
				return deviceEntity;
    }
}
