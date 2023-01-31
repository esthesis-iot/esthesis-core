package esthesis.service.dataflow.impl.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.slugify.Slugify;
import esthesis.common.data.MapUtils;
import esthesis.service.common.BaseService;
import esthesis.service.dataflow.dto.DockerTagsDTO;
import esthesis.service.dataflow.entity.DataflowEntity;
import esthesis.service.dataflow.impl.docker.DockerClient;
import esthesis.service.dataflow.impl.repository.DataflowRepository;
import esthesis.service.kubernetes.dto.PodInfoDTO;
import esthesis.service.kubernetes.resource.KubernetesResource;
import esthesis.service.settings.resource.SettingsResource;
import esthesis.service.tag.resource.TagResource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@ApplicationScoped
public class DataflowService extends BaseService<DataflowEntity> {

  private static final String DOCKER_IMAGE_PREFIX = "esthesisiot/esthesis-dfl-";
  private static final String KUBERNETES_CONTAINER_IMAGE_VERSION = "docker";
  private static final String KUBERNETES_NAMESPACE = "namespace";
  private static final String KUBERNETES_MIN_PODS = "pods-min";
  private static final String KUBERNETES_MAX_PODS = "pods-max";
  private static final String KUBERNETES_CPU_REQUEST = "cpu-request";
  private static final String KUBERNETES_CPU_LIMIT = "cpu-limit";

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

  private Map<String, String> flattenMap(Map<String, Object> map) {
    return map.entrySet().stream().flatMap(MapUtils::flatten)
        .map(entry -> Map.entry(entry.getKey(), entry.getValue().toString()))
        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  /**
   * Converts the configuration options of a dataflow to environmental variables for Kubernetes. Key
   * names are uppercased, and dots and dashes are replaced with underscores.
   *
   * @param map       The configuration options to convert.
   * @param keyPrefix The prefix to add to the keys.
   */
  private Map<String, String> makeEnvironmentVariables(Map<String, String> map, String keyPrefix) {
    return map.entrySet().stream().map(entry -> Map.entry(
        keyPrefix + entry.getKey().toUpperCase().replace(".", "_").replace("-", "_"),
        entry.getValue())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  @Override
  public DataflowEntity save(DataflowEntity dataflowEntity) {
    log.debug("Saving dataflow '{}'.", dataflowEntity);

    // Save the dataflow.
    dataflowEntity = super.save(dataflowEntity);

    // Schedule dataflow in Kubernetes.
    PodInfoDTO podInfoDTO = new PodInfoDTO();
    podInfoDTO.setName(Slugify.builder().build().slugify(dataflowEntity.getName()));
    podInfoDTO.setImage(DOCKER_IMAGE_PREFIX + dataflowEntity.getType());
    podInfoDTO.setVersion(
        (String) dataflowEntity.getKubernetes().get(KUBERNETES_CONTAINER_IMAGE_VERSION));
    podInfoDTO.setNamespace((String) dataflowEntity.getKubernetes().get(KUBERNETES_NAMESPACE));
    podInfoDTO.setMinInstances(
        Integer.parseInt((String) dataflowEntity.getKubernetes().get(KUBERNETES_MIN_PODS)));
    podInfoDTO.setMaxInstances(
        Integer.parseInt((String) dataflowEntity.getKubernetes().get(KUBERNETES_MAX_PODS)));
    podInfoDTO.setCpuRequest((String) dataflowEntity.getKubernetes().get(KUBERNETES_CPU_REQUEST));
    podInfoDTO.setCpuLimit((String) dataflowEntity.getKubernetes().get(KUBERNETES_CPU_LIMIT));
    podInfoDTO.setConfiguration(
        makeEnvironmentVariables(flattenMap(dataflowEntity.getConfig()), "ESTHESIS_DFL_"));
    podInfoDTO.setStatus(dataflowEntity.isStatus());

    kubernetesResource.schedulePod(podInfoDTO);

    return dataflowEntity;
  }

  public DockerTagsDTO getImageTags(String dflType) {
    return dockerClient.getTags(DOCKER_IMAGE_PREFIX + dflType);
  }

  public List<String> getNamespaces() {
    return kubernetesResource.getNamespaces();
  }

  public void delete(String dataflowId) {
    // Remove the dataflow from Kubernetes.
    DataflowEntity dataflow = findById(dataflowId);
    dataflow.setStatus(false);
    save(dataflow);

    // Remove the dataflow from the database.
    deleteById(dataflowId);
  }

}
