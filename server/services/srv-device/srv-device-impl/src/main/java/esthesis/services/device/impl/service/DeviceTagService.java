package esthesis.services.device.impl.service;

import com.google.common.collect.Lists;
import esthesis.service.device.dto.Device;
import esthesis.service.tag.dto.Tag;
import esthesis.service.tag.resource.TagResource;
import esthesis.services.device.impl.repository.DeviceRepository;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
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
   * @param partialMatch Whether the search for the tag name will be partial or
   *                     not.
   * @return Returns the devices matched.
   */
  public List<Device> findByTagName(List<String> tagNames,
      boolean partialMatch) {
    if (tagNames.isEmpty()) {
      return new ArrayList<>();
    } else {
      List<Tag> tagsByName = Lists.newArrayList(
          tagResource.findByNames(String.join(",", tagNames), partialMatch));
      return deviceRepository.findByTagId(tagsByName.stream()
          .map(Tag::getId)
          .map(Object::toString)
          .collect(Collectors.toList()));
    }
  }

  /**
   * Finds the devices matched by the specific list of tags.
   *
   * @param tagName      The tag name to search by.
   * @param partialMatch Whether the search for the tag name will be partial or
   *                     not.
   * @return Returns the devices matched.
   */
  public List<Device> findByTagName(String tagName, boolean partialMatch) {
    return findByTagName(Collections.singletonList(tagName), partialMatch);
  }

  public List<Device> findByTagId(String tagId) {
    return deviceRepository.findByTagId(tagId);
  }

  /**
   * Counts the number of devices having at least one ofe the tags specified.
   *
   * @param tags         The list of tag names to search by.
   * @param partialMatch Whether the search for the tag name should be partial
   *                     or not.
   */
  public Long countByTag(List<String> tags, boolean partialMatch) {
    List<Tag> tagsByName = Lists.newArrayList(
        tagResource.findByNames(String.join(",", tags), partialMatch));
    if (tagsByName.size() > 0) {
      return deviceRepository.countByTag(tagsByName.stream()
          .map(Tag::getId)
          .map(Object::toString)
          .collect(Collectors.toList()));
    } else {
      return 0L;
    }
  }

  /**
   * Removes a tag from all devices having it assigned to them.
   *
   * @param tagName the name of the tag to be removed.
   */
  public void removeTag(String tagName) {
    log.debug("Removing tag '{}' from all devices.", tagName);
    deviceRepository.find("tags", tagName).stream()
        .forEach(device -> {
          device.getTags().removeIf(s -> s.equals(tagName));
          deviceRepository.update(device);
          log.trace("Removed tag '{}' from device '{}'.", tagName,
              device.getId());
        });
  }
}
