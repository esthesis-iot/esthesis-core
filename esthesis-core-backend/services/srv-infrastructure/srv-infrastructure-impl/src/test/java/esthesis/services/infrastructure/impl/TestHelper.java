package esthesis.services.infrastructure.impl;

import esthesis.service.common.paging.Pageable;
import esthesis.service.infrastructure.entity.InfrastructureMqttEntity;
import esthesis.service.tag.entity.TagEntity;
import esthesis.services.infrastructure.impl.repository.InfrastructureMqttRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.UriInfo;
import org.bson.types.ObjectId;
import org.mockito.Mockito;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

@ApplicationScoped
public class TestHelper {

	@Inject
	InfrastructureMqttRepository infrastructureMqttRepository;

	Map<String, TagEntity> mockedTags = Map.of(
		"tag1", makeTag("tag1"),
		"tag2", makeTag("tag2"),
		"tag3", makeTag("tag3"));

	public void createEntities() {
		infrastructureMqttRepository.persist(new InfrastructureMqttEntity(
				"MQTT1",
				"http://localhost.test",
				true,
				List.of(findTagByName("tag1").getId().toString())
			)
		);

		infrastructureMqttRepository.persist(new InfrastructureMqttEntity(
				"MQTT2",
				"http://localhost.test",
				true,
				List.of(findTagByName("tag2").getId().toString())
			)
		);

		infrastructureMqttRepository.persist(new InfrastructureMqttEntity(
			"MQTT3",
			"http://localhost.test",
			true,
			List.of(findTagByName("tag3").getId().toString())
		));


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

	public void clearDatabase() {
		infrastructureMqttRepository.deleteAll();
	}

	public List<InfrastructureMqttEntity> findAllInfrastructureMqttEntity() {
		return infrastructureMqttRepository.listAll();
	}

	public InfrastructureMqttEntity findOneInfrastructureMqttEntity() {
		return findAllInfrastructureMqttEntity().getFirst();
	}

	public InfrastructureMqttEntity findInfrastructureMqttEntityById(String id) {
		return infrastructureMqttRepository.findById(new ObjectId(id));
	}

	public TagEntity makeTag(String name) {
		TagEntity tag = new TagEntity();
		tag.setName(name);
		tag.setId(new ObjectId());
		return tag;
	}

	public TagEntity findTagByName(String tagName) {
		return mockedTags.getOrDefault(tagName, null);
	}
}
