package esthesis.service.dataflow.impl.service;

import com.github.slugify.Slugify;
import esthesis.common.data.DataUtils;
import esthesis.service.common.BaseService;
import esthesis.service.dataflow.dto.DockerTagsDTO;
import esthesis.service.dataflow.entity.DataflowEntity;
import esthesis.service.dataflow.impl.docker.DockerClient;
import esthesis.service.kubernetes.dto.PodInfoDTO;
import esthesis.service.kubernetes.dto.SecretDTO;
import esthesis.service.kubernetes.dto.SecretDTO.SecretDTOBuilder;
import esthesis.service.kubernetes.dto.SecretEntryDTO;
import esthesis.service.kubernetes.resource.KubernetesResource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.microprofile.rest.client.inject.RestClient;

@Slf4j
@Transactional
@ApplicationScoped
public class DataflowService extends BaseService<DataflowEntity> {

	private static final String DOCKER_IMAGE_PREFIX = "esthesisiot/esthesis-core-dfl-";
	private static final String CONTAINER_IMAGE_VERSION = "container-image-version";
	private static final String KUBERNETES_NAMESPACE = "namespace";
	private static final String KUBERNETES_MIN_PODS = "pods-min";
	private static final String KUBERNETES_MAX_PODS = "pods-max";
	private static final String KUBERNETES_CPU_REQUEST = "cpu-request";
	private static final String KUBERNETES_CPU_LIMIT = "cpu-limit";
	private static final String KUBERNETES_IMAGE_REGISTRY_URL = "registry";
	private static final String CUSTOM_ENV_VARS_KEY_NAME = "env";
	private static final String CUSTOM_ENV_VARS_SEPARATOR = "\n";
	private static final String CUSTOM_ENV_VARS_KEY_VALUE_SEPARATOR = "=";
	private static final String SECRET_NAME = "name";
	private static final String SECRET_PATH = "path";
	private static final String SECRET_CONTENT = "content";
	private static final String CONFIG_SECTION_KUBERNETES = "kubernetes";

	@Inject
	@RestClient
	DockerClient dockerClient;

	@Inject
	@RestClient
	KubernetesResource kubernetesResource;

	/**
	 * Flattens the nested keys of a map producing a flat map.
	 *
	 * <pre>
	 *   flattenMap({a={b=1, c=2}, d=3}) = {a.b=1, a.c=2, d=3}
	 * </pre>
	 *
	 * @param map The map to flatten.
	 */
	private Map<String, String> flattenMap(Map<String, Object> map) {
		return map.entrySet().stream().flatMap(DataUtils::flatten)
			.map(entry -> Map.entry(entry.getKey(), entry.getValue().toString()))
			.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
	}

	/**
	 * Converts the configuration options of a dataflow to environmental variables for Kubernetes.
	 *
	 * @param map       The configuration options to convert.
	 * @param keyPrefix The prefix to add to the keys.
	 */
	private Map<String, String> makeEnvironmentVariables(Map<String, String> map, String keyPrefix) {
		Map<String, String> env = new HashMap<>();
		map.forEach((key, value) -> {
			log.info("Evaluate key '{}' with value '{}'.", key, value);
			// Skip pod scheduling-related environmental variables.
			if (key.startsWith(CONFIG_SECTION_KUBERNETES + ".")) {
				log.info("Skipping key '{}' with value '{}'.", key, value);
				return;
			}
			String newKey = keyPrefix + key.toUpperCase().replace(".", "_").replace("-", "_");
			env.put(newKey, value);
			log.info("Adding key '{}' with value '{}' to environment variables.", newKey, value);
		});

		return env;
	}

	/**
	 * Converts user-provided environmental variables to environmental variables for Kubernetes.
	 *
	 * @param envString A string containing the environmental variables.
	 */
	private Map<String, String> addCustomEnvVariables(String envString) {
		Map<String, String> env = new HashMap<>();

		if (StringUtils.isNotEmpty(envString)) {
			String[] envVarsArray = envString.split(CUSTOM_ENV_VARS_SEPARATOR);
			for (String envVar : envVarsArray) {
				String[] envVarArray = envVar.split(CUSTOM_ENV_VARS_KEY_VALUE_SEPARATOR);
				env.put(envVarArray[0], envVarArray[1]);
				log.debug("Adding key '{}' with value '{}' to environment variables.", envVarArray[0],
					envVarArray[1]);
			}
		}

		return env;
	}

	private SecretDTO getSecretSpec(DataflowEntity dataflowEntity) {
		// Create a builder for the secret.
		SecretDTOBuilder builder = SecretDTO.builder().name(dataflowEntity.getName());

		// Get the secrets.
		Map<String, String> flatMap = flattenMap(dataflowEntity.getConfig());

		@SuppressWarnings("unchecked")
		List<Map<String, String>> secrets =
			(List<Map<String, String>>) MapUtils.getMap(
				dataflowEntity.getConfig(), CONFIG_SECTION_KUBERNETES).get("secrets");

		// Iterate over the secrets and add them to the builder.
		if (secrets != null && !secrets.isEmpty()) {
			secrets.forEach(secret -> builder.entry(
        SecretEntryDTO.builder()
          .name(secret.get(SECRET_NAME))
          .path(flatMap.get(SECRET_PATH))
          .content(secret.get(SECRET_CONTENT)).build()
      ));
		}

		return builder.build();
	}

	private PodInfoDTO createPodInfo(DataflowEntity dataflowEntity) {
		PodInfoDTO podInfoDTO = new PodInfoDTO();
		podInfoDTO.setName(Slugify.builder().build().slugify(dataflowEntity.getName()));
		@SuppressWarnings("unchecked")
		Map<String, Object> k8sConfig = (Map<String, Object>)MapUtils.getMap(
			dataflowEntity.getConfig(), CONFIG_SECTION_KUBERNETES);

		if (MapUtils.getString(k8sConfig, KUBERNETES_IMAGE_REGISTRY_URL) != null) {
			podInfoDTO.setImage(MapUtils.getString(k8sConfig, KUBERNETES_IMAGE_REGISTRY_URL) + "/"
				+ DOCKER_IMAGE_PREFIX + dataflowEntity.getType());
		} else {
			podInfoDTO.setImage(DOCKER_IMAGE_PREFIX + dataflowEntity.getType());
		}
		podInfoDTO.setVersion(MapUtils.getString(k8sConfig, CONTAINER_IMAGE_VERSION));
		podInfoDTO.setNamespace(MapUtils.getString (k8sConfig, KUBERNETES_NAMESPACE));
		podInfoDTO.setMinInstances(MapUtils.getIntValue(k8sConfig, KUBERNETES_MIN_PODS));
		podInfoDTO.setMaxInstances(MapUtils.getIntValue(k8sConfig, KUBERNETES_MAX_PODS));
		podInfoDTO.setCpuRequest(MapUtils.getString(k8sConfig, KUBERNETES_CPU_REQUEST));
		podInfoDTO.setCpuLimit(MapUtils.getString(k8sConfig, KUBERNETES_CPU_LIMIT));
		podInfoDTO.setEnvironment(makeEnvironmentVariables(
			flattenMap(dataflowEntity.getConfig()), "ESTHESIS_DFL_"));
		if (MapUtils.getString(k8sConfig, CUSTOM_ENV_VARS_KEY_NAME) != null) {
			podInfoDTO.getEnvironment().putAll(addCustomEnvVariables(
				MapUtils.getString(k8sConfig, CUSTOM_ENV_VARS_KEY_NAME)));
		}
		podInfoDTO.setStatus(dataflowEntity.isStatus());
		podInfoDTO.setSecret(getSecretSpec(dataflowEntity));

		return podInfoDTO;
	}

	@Override
	public DataflowEntity save(DataflowEntity dataflowEntity) {
		log.debug("Saving dataflow '{}'.", dataflowEntity);

		// Save the dataflow.
		dataflowEntity = super.save(dataflowEntity);

		// Schedule dataflow in Kubernetes.
		PodInfoDTO podInfoDTO = createPodInfo(dataflowEntity);
		log.debug("Scheduling pod '{}' in Kubernetes.", podInfoDTO);
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
		// Unschedule dataflow.
		DataflowEntity dataflow = findById(dataflowId);
		dataflow.setStatus(false);
		PodInfoDTO podInfoDTO = createPodInfo(dataflow);
		kubernetesResource.schedulePod(podInfoDTO);

		// Delete dataflow.
		super.delete(dataflow);
	}
}
