package esthesis.service.dataflow.impl.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.slugify.Slugify;
import esthesis.common.AppConstants.DataflowType;
import esthesis.common.AppConstants.Registry;
import esthesis.common.AppConstants.TagsAlgorithm;
import esthesis.common.exception.QMismatchException;
import esthesis.common.service.BaseService;
import esthesis.service.dataflow.dto.DataFlowMqttClientConfig;
import esthesis.service.dataflow.dto.Dataflow;
import esthesis.service.dataflow.dto.DockerTags;
import esthesis.service.dataflow.impl.docker.DockerClient;
import esthesis.service.dataflow.impl.repository.DataflowRepository;
import esthesis.service.kubernetes.dto.PodInfo;
import esthesis.service.kubernetes.resource.KubernetesResource;
import esthesis.service.registry.resource.RegistryResource;
import esthesis.service.tag.resource.TagResource;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.eclipse.microprofile.config.ConfigProvider;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class DataflowService extends BaseService<Dataflow> {

  @Inject
  @RestClient
  RegistryResource registryResource;

  @Inject
  @RestClient
  DockerClient dockerClient;

  @Inject
  @RestClient
  TagResource tagResource;

  @Inject
  @RestClient
  KubernetesResource kubernetesResource;

  @Inject
  DataflowRepository dataflowRepository;

  @Inject
  ObjectMapper objectMapper;

  /**
   * Finds an MQTT server with the given tags.
   *
   * @return Returns the MQTT server registered with all given tags matched.
   */
  public DataFlowMqttClientConfig matchMqttServerByTags(List<String> tagsList) {
    Optional<Dataflow> mqttDataflow = Optional.empty();

    // Check whether an MQTT server matches according to the tag matching algorithm.
    TagsAlgorithm deviceTagsAlgorithm = TagsAlgorithm.valueOf(
        registryResource.findByName(
            Registry.DEVICE_TAGS_ALGORITHM).asString());

    if (tagsList.isEmpty()) {
      mqttDataflow = dataflowRepository.findByType(
              DataflowType.MQTT_CLIENT)
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
      switch (deviceTagsAlgorithm) {
        case ALL -> {
          mqttDataflow = dataflowRepository.findByType(
                  DataflowType.MQTT_CLIENT)
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
                  DataflowType.MQTT_CLIENT)
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
            DataFlowMqttClientConfig.class);
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

  @Override
  public Dataflow save(Dataflow dataflow) {
    // Get the current state of this dataflow, if it exists.
    Dataflow existingDataflow = findById(dataflow.getId());

    // Get the configuration for this pod.
    Map<String, String> configMap = new HashMap<>();
    try {
      LinkedHashMap<String, Object> config = objectMapper.readValue(
          dataflow.getConfiguration(), new TypeReference<>() {
          });
      config.keySet().iterator().forEachRemaining(key -> {
        configMap.put(key, config.get(key).toString());
      });
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }

    // Schedule dataflow in Kubernetes.
    PodInfo podInfo = new PodInfo();
//    if (existingDataflow == null) {
    podInfo.setName(Slugify.builder().build().slugify(dataflow.getName()));
    podInfo.setImage(ConfigProvider.getConfig()
        .getValue("esthesis.docker.dataflows." + dataflow.getType(),
            String.class));
    podInfo.setVersion(dataflow.getVersion());
    podInfo.setNamespace(dataflow.getNamespace());
    podInfo.setMinInstances(dataflow.getMinPods());
    podInfo.setMaxInstances(dataflow.getMaxPods());
    podInfo.setConfiguration(configMap);

    Boolean response = kubernetesResource.startPod(podInfo);
//    } else {
//
//    }

    // Save the dataflow.
    dataflow = super.save(dataflow);

    return dataflow;
  }

  public DockerTags getImageTags(String dflType) {
    return dockerClient.getTags(ConfigProvider.getConfig()
        .getValue("esthesis.docker.dataflows." + dflType,
            String.class));
  }

  public List<String> getNamespaces() {
    return kubernetesResource.getNamespaces();
  }
}
