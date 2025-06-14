package esthesis.services.device.impl.service;

import static esthesis.core.common.AppConstants.Security.Category.DEVICE;
import static esthesis.core.common.AppConstants.Security.Operation.READ;
import static esthesis.core.common.AppConstants.Security.Operation.WRITE;

import com.google.common.collect.Lists;
import esthesis.core.common.AppConstants.Device.Status;
import esthesis.service.device.entity.DeviceEntity;
import esthesis.service.security.annotation.ErnPermission;
import esthesis.service.tag.entity.TagEntity;
import esthesis.service.tag.resource.TagResource;
import esthesis.services.device.impl.repository.DeviceRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;

/**
 * Service for managing device tags.
 */
@Slf4j
@Transactional
@ApplicationScoped
public class DeviceTagService {

	@Inject
	DeviceRepository deviceRepository;

	@Inject
	@RestClient
	TagResource tagResource;


	/**
	 * Finds the devices matched by the specific list of tags.v
	 *
	 * @param tagNames The list of tag names to search by, separated by comma.
	 * @return Returns the devices matched.
	 */
	@ErnPermission(category = DEVICE, operation = READ)
	public List<DeviceEntity> findByTagName(List<String> tagNames) {
		if (CollectionUtils.isEmpty(tagNames)) {
			return new ArrayList<>();
		} else {
			List<TagEntity> tagsByName = Lists.newArrayList(
				tagResource.findByNames(String.join(",", tagNames)));
			return deviceRepository.findByTagId(tagsByName.stream()
				.map(TagEntity::getId)
				.map(Object::toString)
				.toList());
		}
	}

	/**
	 * Finds the devices matched by the specific list of tags.
	 *
	 * @param tagName The tag name to search by.
	 * @return Returns the devices matched.
	 */
	@ErnPermission(category = DEVICE, operation = READ)
	public List<DeviceEntity> findByTagName(String tagName) {
		return findByTagName(Collections.singletonList(tagName));
	}

	/**
	 * Finds the devices matched by the specific list of tags.
	 *
	 * @param tagId The tag id to search by.
	 * @return Returns the devices matched.
	 */
	@ErnPermission(category = DEVICE, operation = READ)
	public List<DeviceEntity> findByTagId(String tagId) {
		return deviceRepository.findByTagId(tagId);
	}

	/**
	 * Finds the devices matched by the specific list of tags.
	 *
	 * @param tagIds The list of tag ids to search by.
	 * @return Returns the devices matched.
	 */
	@ErnPermission(category = DEVICE, operation = READ)
	public List<DeviceEntity> findByTagId(List<String> tagIds) {
		return deviceRepository.findByTagId(tagIds);
	}

	/**
	 * Counts the number of devices having at least one of the tags specified.
	 *
	 * @param tags The list of tag names to search by.
	 * @return Returns the number of devices matched.
	 */
	@ErnPermission(category = DEVICE, operation = READ)
	public Long countByTag(List<String> tags) {
		List<TagEntity> tagsByName = Lists.newArrayList(
			tagResource.findByNames(String.join(",", tags)));
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
	@ErnPermission(category = DEVICE, operation = WRITE)
	public void removeTagById(String tagId) {
		log.debug("Removing tag id '{}' from all devices.", tagId);
		deviceRepository.find("tags", tagId).stream()
			.forEach(device -> {
				device.getTags().removeIf(s -> s.equals(tagId));
				deviceRepository.update(device);
				log.debug("Removed tag id '{}' from device '{}'.", tagId, device.getId());
			});
	}

	@ErnPermission(category = DEVICE, operation = READ)
  public Long countByStatus(Status status) {
		return deviceRepository.countByStatus(status);
  }
}
