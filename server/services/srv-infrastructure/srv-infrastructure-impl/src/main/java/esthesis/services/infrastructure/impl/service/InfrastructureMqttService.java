package esthesis.services.infrastructure.impl.service;

import esthesis.service.common.BaseService;
import esthesis.service.infrastructure.entity.InfrastructureMqttEntity;
import esthesis.service.tag.resource.TagSystemResource;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
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
    if (tagsList.isEmpty()) {
      match = findByColumnNull("tags").stream().findAny();
    } else {
      match = findByColumnIn("tags", tagIds, false).stream().findAny();
    }

    return match;
  }
}
