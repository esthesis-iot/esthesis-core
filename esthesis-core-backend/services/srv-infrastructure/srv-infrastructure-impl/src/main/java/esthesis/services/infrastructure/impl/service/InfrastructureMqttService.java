package esthesis.services.infrastructure.impl.service;

import static esthesis.core.common.AppConstants.Security.Category.INFRASTRUCTURE;
import static esthesis.core.common.AppConstants.Security.Operation.CREATE;
import static esthesis.core.common.AppConstants.Security.Operation.DELETE;
import static esthesis.core.common.AppConstants.Security.Operation.READ;
import static esthesis.core.common.AppConstants.Security.Operation.WRITE;

import esthesis.service.common.BaseService;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.infrastructure.entity.InfrastructureMqttEntity;
import esthesis.service.security.annotation.ErnPermission;
import esthesis.service.tag.entity.TagEntity;
import esthesis.service.tag.resource.TagSystemResource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Objects;
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

	private InfrastructureMqttEntity saveHandler(InfrastructureMqttEntity entity) {
		return super.save(entity);
	}

	@ErnPermission(category = INFRASTRUCTURE, operation = READ)
	public Optional<InfrastructureMqttEntity> matchByTags(String tags) {
		log.debug("Looking for a matching MQTT server for tags '{}'.", tags);

		// Convert the names of the tags to their IDs.
		List<String> tagsList = List.of(tags.split(","));

		// Find the MQTT server matching the tags:
		// - If no tags are provided, return a random MQTT server.
		// - If no matching tags is found, return a random MQTT server.
		Optional<InfrastructureMqttEntity> match;
		if (CollectionUtils.isEmpty(tagsList)) {
			match = findRandom();
		} else {
			final List<String> tagIds = tagsList.stream()
				.map(tagName -> {
					TagEntity tagResult = tagSystemResource.findByName(tagName);
					return (tagResult != null) ? tagResult.getId().toString() : null;
				})
				.filter(Objects::nonNull)
				.toList();

			if (tagIds.isEmpty()) {
				match = findRandom();
			} else {
				match = findByColumnIn("tags", tagIds, false).stream().findAny();
			}
		}

		log.debug("Returning MQTT server '{}'.", match);

		return match;
	}

	@ErnPermission(category = INFRASTRUCTURE, operation = READ)
	public Optional<InfrastructureMqttEntity> matchRandom() {
		log.debug("Looking for a random MQTT server.");

		// Find a random MQTT server.
		Optional<InfrastructureMqttEntity> match = findRandom();

		log.debug("Returning MQTT server '{}'.", match);

		return match;
	}

	@Override
	@ErnPermission(category = INFRASTRUCTURE, operation = READ)
	public Page<InfrastructureMqttEntity> find(Pageable pageable, boolean partialMatch) {
		return super.find(pageable, partialMatch);
	}

	@Override
	@ErnPermission(category = INFRASTRUCTURE, operation = READ)
	public InfrastructureMqttEntity findById(String id) {
		return super.findById(id);
	}

	@ErnPermission(category = INFRASTRUCTURE, operation = CREATE)
	public InfrastructureMqttEntity saveNew(InfrastructureMqttEntity entity) {
		return saveHandler(entity);
	}

	@ErnPermission(category = INFRASTRUCTURE, operation = WRITE)
	public InfrastructureMqttEntity saveUpdate(InfrastructureMqttEntity entity) {
		return saveHandler(entity);
	}

	@Override
	@ErnPermission(category = INFRASTRUCTURE, operation = DELETE)
	public boolean deleteById(String deviceId) {
		return super.deleteById(deviceId);
	}
}
