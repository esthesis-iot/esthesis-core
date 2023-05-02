package esthesis.services.device.impl.service;

import com.google.common.collect.Lists;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.tag.entity.TagEntity;
import esthesis.service.tag.resource.TagResource;
import esthesis.services.device.impl.repository.DeviceRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class DeviceTagService {

	@Inject
	DeviceRepository deviceRepository;

	@Inject
	@RestClient
	TagResource tagResource;

	/**
	 * Finds the devices matched by the specific list of tags.
	 *
	 * @param tagNames     The list of tag names to search by.
	 * @param partialMatch Whether the search for the tag name will be partial or not.
	 * @return Returns the devices matched.
	 */
	public List<DeviceEntity> findByTagName(List<String> tagNames,
		boolean partialMatch) {
		if (CollectionUtils.isEmpty(tagNames)) {
			return new ArrayList<>();
		} else {
			List<TagEntity> tagsByName = Lists.newArrayList(
				tagResource.findByNames(String.join(",", tagNames), partialMatch));
			return deviceRepository.findByTagId(tagsByName.stream()
				.map(TagEntity::getId)
				.map(Object::toString)
				.toList());
		}
	}

	/**
	 * Finds the devices matched by the specific list of tags.
	 *
	 * @param tagName      The tag name to search by.
	 * @param partialMatch Whether the search for the tag name will be partial or not.
	 * @return Returns the devices matched.
	 */
	public List<DeviceEntity> findByTagName(String tagName, boolean partialMatch) {
		return findByTagName(Collections.singletonList(tagName), partialMatch);
	}

	public List<DeviceEntity> findByTagId(String tagId) {
		return deviceRepository.findByTagId(tagId);
	}

	/**
	 * Counts the number of devices having at least one of the tags specified.
	 *
	 * @param tags         The list of tag names to search by.
	 * @param partialMatch Whether the search for the tag name should be partial or not.
	 */
	public Long countByTag(List<String> tags, boolean partialMatch) {
		List<TagEntity> tagsByName = Lists.newArrayList(
			tagResource.findByNames(String.join(",", tags), partialMatch));
		if (!CollectionUtils.isEmpty(tagsByName)) {
			return deviceRepository.countByTag(tagsByName.stream()
				.map(TagEntity::getId)
				.map(Object::toString)
				.toList());
		} else {
			return 0L;
		}
	}

	/**
	 * Removes a tag from all devices having it assigned to them.
	 *
	 * @param tagId the ID of the tag to be removed.
	 */
	public void removeTagById(String tagId) {
		log.debug("Removing tag id '{}' from all devices.", tagId);
		deviceRepository.find("tags", tagId).stream()
			.forEach(device -> {
				device.getTags().removeIf(s -> s.equals(tagId));
				deviceRepository.update(device);
				log.debug("Removed tag id '{}' from device '{}'.", tagId, device.getId());
			});
	}
}
