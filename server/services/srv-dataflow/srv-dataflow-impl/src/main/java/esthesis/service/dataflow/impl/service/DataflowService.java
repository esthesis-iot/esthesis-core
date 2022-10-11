package esthesis.service.dataflow.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.slugify.Slugify;
import esthesis.common.AppConstants.Registry;
import esthesis.common.AppConstants.TagsAlgorithm;
import esthesis.common.service.BaseService;
import esthesis.common.util.MapUtils;
import esthesis.service.dataflow.dto.DataFlowMqttClientConfig;
import esthesis.service.dataflow.dto.Dataflow;
import esthesis.service.dataflow.dto.DockerTags;
import esthesis.service.dataflow.impl.docker.DockerClient;
import esthesis.service.dataflow.impl.repository.DataflowRepository;
import esthesis.service.kubernetes.dto.PodInfo;
import esthesis.service.kubernetes.resource.KubernetesResource;
import esthesis.service.registry.resource.RegistryResource;
import esthesis.service.tag.resource.TagResource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@Transactional
@ApplicationScoped
public class DataflowService extends BaseService<Dataflow> {

  private static final String DOCKER_IMAGE_PREFIX = "esthesisiot/esthesis-dfl-";
  private static final String KUBERNETES_CONTAINER_IMAGE_VERSION = "docker";
  private static final String KUBERNETES_NAMESPACE = "namespace";
  private static final String KUBERNETES_MIN_PODS = "pods-min";
  private static final String KUBERNETES_MAX_PODS = "pods-max";
  private static final String KUBERNETES_CPU_REQUEST = "cpu-request";
  private static final String KUBERNETES_CPU_LIMIT = "cpu-limit";

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

//    if (tagsList.isEmpty()) {
//      mqttDataflow = dataflowRepository.findByType(
//              DataflowType.MQTT_CLIENT)
//          .stream()
//          .filter(Dataflow::isStatus)
//          .filter(dataflow -> {
//            try {
//              return objectMapper.readValue(dataflow.getConfiguration(),
//                  DataFlowMqttClientConfig.class).getTags().isEmpty();
//            } catch (JsonProcessingException e) {
//              throw new QMismatchException(
//                  "Could not parse MQTT server configuration.", e);
//            }
//          })
//          .findAny();
//    } else {
//      switch (deviceTagsAlgorithm) {
//        case ALL -> {
//          mqttDataflow = dataflowRepository.findByType(
//                  DataflowType.MQTT_CLIENT)
//              .stream()
//              .filter(Dataflow::isStatus)
//              .filter(dataflow -> {
//                try {
//                  return objectMapper.readValue(dataflow.getConfiguration(),
//                          DataFlowMqttClientConfig.class).getTags()
//                      .containsAll(tagsList);
//                } catch (JsonProcessingException e) {
//                  throw new QMismatchException(
//                      "Could not parse MQTT server configuration.", e);
//                }
//              })
//              .findAny();
//        }
//        case ANY -> {
//          mqttDataflow = dataflowRepository.findByType(
//                  DataflowType.MQTT_CLIENT)
//              .stream()
//              .filter(Dataflow::isStatus)
//              .filter(dataflow -> {
//                try {
//                  return ListUtils.intersection(
//                          objectMapper.readValue(dataflow.getConfiguration(),
//                              DataFlowMqttClientConfig.class).getTags(), tagsList)
//                      .size() > 0;
//                } catch (JsonProcessingException e) {
//                  throw new QMismatchException(
//                      "Could not parse MQTT server configuration.", e);
//                }
//              })
//              .findAny();
//        }
//      }
//    }

//    if (mqttDataflow.isPresent()) {
//      try {
//        return objectMapper.readValue(mqttDataflow.get().getConfiguration(),
//            DataFlowMqttClientConfig.class);
//      } catch (JsonProcessingException e) {
//        throw new QMismatchException(
//            "Could not parse MQTT server configuration.", e);
//      }
//    } else {
//      log.warn("MQTT server match not found (tags: {}, algorithm: {}).",
//          tagsList,
//          deviceTagsAlgorithm);
//      return null;
//    }

    return null;
  }

  private Map<String, String> flattenMap(Map<String, Object> map) {
    return map.entrySet().stream()
        .flatMap(MapUtils::flatten)
        .map(entry -> Map.entry(entry.getKey(), entry.getValue().toString()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private Map<String, String> makeEnvironmentVariables(
      Map<String, String> map, String keyPrefix) {
    return map.entrySet().stream()
        .map(entry -> Map.entry(
            keyPrefix + entry.getKey().toUpperCase().replace(".",
                "_").replace("-", "_"), entry.getValue()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Override
  public Dataflow save(Dataflow dataflow) {
    // Get the current state of this dataflow, if it exists.
    if (dataflow.getId() != null) {
      Dataflow existingDataflow = findById(dataflow.getId());
    }

    // Save the dataflow.
    dataflow = super.save(dataflow);

    // Get Kubernetes configuration.
    Map<String, String> kubernetesConfig = flattenMap(dataflow.getKubernetes());

    // Schedule dataflow in Kubernetes.
    PodInfo podInfo = new PodInfo();
    podInfo.setName(Slugify.builder().build().slugify(dataflow.getName()));
    podInfo.setImage(DOCKER_IMAGE_PREFIX + dataflow.getType());
    podInfo.setVersion((String) dataflow.getKubernetes()
        .get(KUBERNETES_CONTAINER_IMAGE_VERSION));
    podInfo.setNamespace(
        (String) dataflow.getKubernetes().get(KUBERNETES_NAMESPACE));
    podInfo.setMinInstances(
        Integer.parseInt(
            (String) dataflow.getKubernetes().get(KUBERNETES_MIN_PODS)));
    podInfo.setMaxInstances(
        Integer.parseInt(
            (String) dataflow.getKubernetes().get(KUBERNETES_MAX_PODS)));
    podInfo.setCpuRequest(
        (String) dataflow.getKubernetes().get(KUBERNETES_CPU_REQUEST));
    podInfo.setCpuLimit(
        (String) dataflow.getKubernetes().get(KUBERNETES_CPU_LIMIT));
    podInfo.setConfiguration(
        makeEnvironmentVariables(flattenMap(dataflow.getConfig()),
            "ESTHESIS_DFL_"));
    podInfo.setStatus(dataflow.isStatus());

    kubernetesResource.schedulePod(podInfo);

    return dataflow;
  }

  public DockerTags getImageTags(String dflType) {
    return dockerClient.getTags(DOCKER_IMAGE_PREFIX + dflType);
  }

  public List<String> getNamespaces() {
    return kubernetesResource.getNamespaces();
  }
}
