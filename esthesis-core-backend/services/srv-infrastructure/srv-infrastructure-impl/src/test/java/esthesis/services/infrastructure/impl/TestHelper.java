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

	public InfrastructureMqttEntity createInfrastructureMQtt(String name, String url, boolean active, String tag) {
		return new InfrastructureMqttEntity(
			name,
			url,
			active, List.of(findTagByName(tag).getId().toString())
		);
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

	public void clearDatabase() {
		infrastructureMqttRepository.deleteAll();
	}

	public List<InfrastructureMqttEntity> findAllInfrastructureMqttEntity() {
		return infrastructureMqttRepository.listAll();
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
