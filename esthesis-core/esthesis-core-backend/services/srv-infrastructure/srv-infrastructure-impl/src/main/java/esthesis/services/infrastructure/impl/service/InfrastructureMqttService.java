package esthesis.services.infrastructure.impl.service;

import esthesis.service.common.BaseService;
import esthesis.service.infrastructure.entity.InfrastructureMqttEntity;
import esthesis.service.tag.resource.TagSystemResource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@Transactional
@ApplicationScoped
public class InfrastructureMqttService extends BaseService<InfrastructureMqttEntity> {

	@Inject
	@RestClient
	TagSystemResource tagSystemResource;

	public Optional<InfrastructureMqttEntity> matchByTags(String tags) {

		log.debug("Looking for a matching MQTT server for tags '{}'.", tags);

		// Convert the names of the tags to their IDs.
		List<String> tagsList = List.of(tags.split(","));
		final List<String> tagIds = tagsList.stream()
			.map(tagName -> tagSystemResource.findByName(tagName).getId()
				.toString())
			.toList();

		Optional<InfrastructureMqttEntity> match;
		if (CollectionUtils.isEmpty(tagsList)) {
			match = findByColumnNull("tags").stream().findAny();
		} else {
			match = findByColumnIn("tags", tagIds, false).stream().findAny();
		}

		return match;
	}
}
