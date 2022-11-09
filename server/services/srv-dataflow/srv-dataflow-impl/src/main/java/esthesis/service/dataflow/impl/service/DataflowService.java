package esthesis.service.dataflow.impl.service;

import static esthesis.common.AppConstants.DFL_MQTT_CLIENT_NAME;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.slugify.Slugify;
import esthesis.common.AppConstants.NamedSetting;
import esthesis.common.AppConstants.TagsAlgorithm;
import esthesis.common.data.MapUtils;
import esthesis.service.common.BaseService;
import esthesis.service.dataflow.dto.Dataflow;
import esthesis.service.dataflow.dto.DockerTags;
import esthesis.service.dataflow.dto.MatchedMqttServer;
import esthesis.service.dataflow.impl.docker.DockerClient;
import esthesis.service.dataflow.impl.repository.DataflowRepository;
import esthesis.service.kubernetes.dto.PodInfo;
import esthesis.service.kubernetes.resource.KubernetesResource;
import esthesis.service.settings.resource.SettingsResource;
import esthesis.service.tag.resource.TagResource;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.bson.Document;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class DataflowService extends BaseService<Dataflow> {

  private static final String DOCKER_IMAGE_PREFIX = "esthesisiot/esthesis-dfl-";
  private static final String KUBERNETES_CONTAINER_IMAGE_VERSION = "docker";
  private static final String KUBERNETES_NAMESPACE = "namespace";
  private static final String KUBERNETES_MIN_PODS = "pods-min";
  private static final String KUBERNETES_MAX_PODS = "pods-max";
  private static final String KUBERNETES_CPU_REQUEST = "cpu-request";
  private static final String KUBERNETES_CPU_LIMIT = "cpu-limit";

  // As defined in `redis.ts` under `dataflow-definitions`.
  public static final String REDIS_DATAFLOW_TYPE = "redis-cache";

  @Inject
  @RestClient
  SettingsResource settingsResource;

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
   * @param tagNames the tag names to search by.
   * @return Returns the MQTT server registered with all given tags matched.
   */
  public MatchedMqttServer matchMqttServerByTags(List<String> tagNames) {
    MatchedMqttServer matchedMqttServer = new MatchedMqttServer();

    // The dataflow type representing an MQTT client.
    final String MQTT_BROKER = "mqtt-broker";
    final String TAGS = "tags";
    final String MQTT_BROKER_ADVERTISED_URL = "advertised-url";

    // Convert the names of the tags to their IDs.
    log.debug("Looking for a matching MQTT server for tags '{}'.", tagNames);
    final List<String> tagIds = tagNames.stream()
        .map(tagName -> tagResource.findByName(tagName, false).getId()
            .toString())
        .toList();

    // Find all dataflows of type MQTT client.
    List<Dataflow> dataflows = dataflowRepository.findByType(
        DFL_MQTT_CLIENT_NAME);

    // Find the matching algorithm to use.
    TagsAlgorithm deviceTagsAlgorithm = TagsAlgorithm.valueOf(
        settingsResource.findByName(
            NamedSetting.DEVICE_TAGS_ALGORITHM).asString());

    Optional<Dataflow> match = Optional.empty();
    if (tagNames.isEmpty()) {
      match = dataflows.stream().filter(Dataflow::isStatus)
          .filter(dataflow -> {
            return
                ((Document) dataflows.get(0).getConfig().get(MQTT_BROKER)).get(
                    TAGS) == null;
          })
          .findAny();
    } else {
      switch (deviceTagsAlgorithm) {
        case ALL -> {
          match = dataflows.stream().filter(Dataflow::isStatus)
              .filter(dataflow -> {
                return ((Document) dataflows.get(0).getConfig()
                    .get(MQTT_BROKER)).getList(
                    TAGS, String.class).containsAll(tagIds);
              })
              .findAny();
        }
        case ANY -> {
          match = dataflows.stream().filter(Dataflow::isStatus)
              .filter(dataflow -> {
                return ListUtils.intersection(
                        ((Document) dataflows.get(0).getConfig()
                            .get(MQTT_BROKER)).getList(
                            TAGS, String.class), tagIds)
                    .size() > 0;
              })
              .findAny();
        }
      }
    }

    if (match.isPresent()) {
      matchedMqttServer.setUrl(
          ((Document) match.get().getConfig().get(MQTT_BROKER)).get(
              MQTT_BROKER_ADVERTISED_URL).toString());
      matchedMqttServer.setMatchingAlgorithm(deviceTagsAlgorithm);
      matchedMqttServer.setTagsUsed(tagNames);
      log.debug("Found match for MQTT server '{}'.", matchedMqttServer);
    } else {
      log.debug("No match found for MQTT server.");
    }

    return matchedMqttServer;
  }

  private Map<String, String> flattenMap(Map<String, Object> map) {
    return map.entrySet().stream()
        .flatMap(MapUtils::flatten)
        .map(entry -> Map.entry(entry.getKey(), entry.getValue().toString()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /**
   * Converts the configuration options of a dataflow to environmental variables
   * for Kubernetes. Key names are uppercased, and dots and dashes are replaced
   * with underscores.
   *
   * @param map       The configuration options to convert.
   * @param keyPrefix The prefix to add to the keys.
   */
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

  public List<Dataflow> getRedisSetup() {
    return dataflowRepository.findByType(REDIS_DATAFLOW_TYPE);
  }
}
