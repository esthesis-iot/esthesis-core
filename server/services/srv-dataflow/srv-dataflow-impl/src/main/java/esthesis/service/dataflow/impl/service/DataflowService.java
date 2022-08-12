package esthesis.service.dataflow.impl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.common.AppConstants;
import esthesis.common.AppConstants.Device.TAGS_ALGORITHM;
import esthesis.common.AppConstants.Registry;
import esthesis.common.exception.QMismatchException;
import esthesis.common.service.BaseService;
import esthesis.service.dataflow.dto.DataFlowMqttClientConfig;
import esthesis.service.dataflow.dto.Dataflow;
import esthesis.service.dataflow.impl.repository.DataflowRepository;
import esthesis.service.registry.resource.RegistryResourceV1;
import esthesis.service.tag.dto.Tag;
import esthesis.service.tag.resource.TagResourceV1;
import java.util.List;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.bson.types.ObjectId;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class DataflowService extends BaseService<Dataflow> {

  //  @Inject
//  JsonWebToken jwt;
  @Inject
  @RestClient
  RegistryResourceV1 registryResourceV1;

  @Inject
  @RestClient
  TagResourceV1 tagResourceV1;

  @Inject
  DataflowRepository dataflowRepository;

  @Inject
  ObjectMapper objectMapper;

  /**
   * Finds an MQTT server with the given tags.
   *
   * @return Returns the MQTT server registered with all given tags matched.
   */
  public String matchMqttServerByTags(List<String> tagsList) {
    Optional<Dataflow> mqttDataflow = Optional.empty();

    // Check whether an MQTT server matches according to the tag matching algorithm.
    TAGS_ALGORITHM deviceTagsAlgorithm = TAGS_ALGORITHM.valueOf(
        registryResourceV1.findByName(
            Registry.DEVICE_TAGS_ALGORITHM).asString());

    if (tagsList.isEmpty()) {
      mqttDataflow = dataflowRepository.findByType(
              AppConstants.Dataflow.MQTT_CLIENT)
          .stream()
          .filter(Dataflow::isStatus)
          .filter(dataflow -> {
            try {
              return objectMapper.readValue(dataflow.getConfiguration(),
                  DataFlowMqttClientConfig.class).getTags().isEmpty();
            } catch (JsonProcessingException e) {
              throw new QMismatchException(
                  "Could not parse MQTT server configuration.", e);
            }
          })
          .findAny();
    } else {
      // Convert tag names to tag Ids and find matching registered MQTT servers.
      List<ObjectId> tagIds = tagsList.stream().map(tagResourceV1::findByName)
          .map(Tag::getId).toList();

      switch (deviceTagsAlgorithm) {
        case ALL -> {
          mqttDataflow = dataflowRepository.findByType(
                  AppConstants.Dataflow.MQTT_CLIENT)
              .stream()
              .filter(Dataflow::isStatus)
              .filter(dataflow -> {
                try {
                  return objectMapper.readValue(dataflow.getConfiguration(),
                          DataFlowMqttClientConfig.class).getTags()
                      .containsAll(tagsList);
                } catch (JsonProcessingException e) {
                  throw new QMismatchException(
                      "Could not parse MQTT server configuration.", e);
                }
              })
              .findAny();
        }
        case ANY -> {
          mqttDataflow = dataflowRepository.findByType(
                  AppConstants.Dataflow.MQTT_CLIENT)
              .stream()
              .filter(Dataflow::isStatus)
              .filter(dataflow -> {
                try {
                  return ListUtils.intersection(
                          objectMapper.readValue(dataflow.getConfiguration(),
                              DataFlowMqttClientConfig.class).getTags(), tagsList)
                      .size() > 0;
                } catch (JsonProcessingException e) {
                  throw new QMismatchException(
                      "Could not parse MQTT server configuration.", e);
                }
              })
              .findAny();
        }
      }
    }

    if (mqttDataflow.isPresent()) {
      try {
        return objectMapper.readValue(mqttDataflow.get().getConfiguration(),
            DataFlowMqttClientConfig.class).getUrl();
      } catch (JsonProcessingException e) {
        throw new QMismatchException(
            "Could not parse MQTT server configuration.", e);
      }
    } else {
      log.warn("MQTT server match not found (tags: {}, algorithm: {}).",
          tagsList,
          deviceTagsAlgorithm);
      return null;
    }
  }

}
