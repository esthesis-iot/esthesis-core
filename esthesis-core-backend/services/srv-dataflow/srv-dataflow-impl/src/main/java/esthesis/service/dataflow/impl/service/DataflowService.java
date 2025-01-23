package esthesis.service.dataflow.impl.service;

import static esthesis.core.common.AppConstants.Security.Category.DATAFLOW;
import static esthesis.core.common.AppConstants.Security.Operation.CREATE;
import static esthesis.core.common.AppConstants.Security.Operation.DELETE;
import static esthesis.core.common.AppConstants.Security.Operation.READ;
import static esthesis.core.common.AppConstants.Security.Operation.WRITE;

import com.github.slugify.Slugify;
import esthesis.common.data.DataUtils;
import esthesis.service.common.BaseService;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.dataflow.dto.FormlySelectOption;
import esthesis.service.dataflow.entity.DataflowEntity;
import esthesis.service.dataflow.impl.docker.DockerClient;
import esthesis.service.kubernetes.dto.DeploymentInfoDTO;
import esthesis.service.kubernetes.dto.SecretDTO;
import esthesis.service.kubernetes.dto.SecretDTO.SecretDTOBuilder;
import esthesis.service.kubernetes.dto.SecretEntryDTO;
import esthesis.service.kubernetes.resource.KubernetesResource;
import esthesis.service.security.annotation.ErnPermission;
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

/**
 * Service for managing dataflows.
 */
@Slf4j
@Transactional
@ApplicationScoped
public class DataflowService extends BaseService<DataflowEntity> {

	private static final String DOCKER_IMAGE_PREFIX = "esthesis-core-dfl-";
	private static final String DOCKER_IMAGE_DEFAULT_URL = "esthesisiot";
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
	 * @return The flattened map.
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
	 * @return The environmental variables.
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
	 * @return The environmental variables.
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

	/**
	 * Creates a secret specification DTO for a dataflow.
	 *
	 * @param dataflowEntity The dataflow entity.
	 * @return The secret specification.
	 */
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

	/**
	 * Creates the URL for the Docker image of a dataflow.
	 *
	 * @param k8sConfig The Kubernetes configuration.
	 * @param dflType   The type of the dataflow.
	 * @return The URL of the Docker image.
	 */
	private String createDflImageUrl(Map<String, Object> k8sConfig, String dflType) {
		if (StringUtils.isNotBlank(MapUtils.getString(k8sConfig, KUBERNETES_IMAGE_REGISTRY_URL))) {
			return MapUtils.getString(k8sConfig, KUBERNETES_IMAGE_REGISTRY_URL) + "/"
				+ DOCKER_IMAGE_PREFIX + dflType;
		} else {
			return DOCKER_IMAGE_DEFAULT_URL + "/" + DOCKER_IMAGE_PREFIX + dflType;
		}
	}

	/**
	 * Creates a deployment information DTO for a dataflow.
	 *
	 * @param dataflowEntity The dataflow entity.
	 * @return The deployment information DTO.
	 */
	private DeploymentInfoDTO createPodInfo(DataflowEntity dataflowEntity) {
		DeploymentInfoDTO deploymentInfoDTO = new DeploymentInfoDTO();
		deploymentInfoDTO.setName(Slugify.builder().build().slugify(dataflowEntity.getName()));
		@SuppressWarnings("unchecked")
		Map<String, Object> k8sConfig = (Map<String, Object>) MapUtils.getMap(
			dataflowEntity.getConfig(), CONFIG_SECTION_KUBERNETES);
		deploymentInfoDTO.setImage(createDflImageUrl(k8sConfig, dataflowEntity.getType()));
		deploymentInfoDTO.setVersion(MapUtils.getString(k8sConfig, CONTAINER_IMAGE_VERSION));
		deploymentInfoDTO.setNamespace(MapUtils.getString(k8sConfig, KUBERNETES_NAMESPACE));
		deploymentInfoDTO.setMinInstances(MapUtils.getIntValue(k8sConfig, KUBERNETES_MIN_PODS));
		deploymentInfoDTO.setMaxInstances(MapUtils.getIntValue(k8sConfig, KUBERNETES_MAX_PODS));
		deploymentInfoDTO.setCpuRequest(MapUtils.getString(k8sConfig, KUBERNETES_CPU_REQUEST));
		deploymentInfoDTO.setCpuLimit(MapUtils.getString(k8sConfig, KUBERNETES_CPU_LIMIT));
		deploymentInfoDTO.setEnvironment(makeEnvironmentVariables(
			flattenMap(dataflowEntity.getConfig()), "ESTHESIS_DFL_"));
		if (MapUtils.getString(k8sConfig, CUSTOM_ENV_VARS_KEY_NAME) != null) {
			deploymentInfoDTO.getEnvironment().putAll(addCustomEnvVariables(
				MapUtils.getString(k8sConfig, CUSTOM_ENV_VARS_KEY_NAME)));
		}
		deploymentInfoDTO.setStatus(dataflowEntity.isStatus());
		deploymentInfoDTO.setSecret(getSecretSpec(dataflowEntity));

		return deploymentInfoDTO;
	}

	/**
	 * Saves a dataflow entity.
	 *
	 * @param dataflowEntity The dataflow entity to save.
	 * @return The saved dataflow entity.
	 */
	private DataflowEntity saveHandler(DataflowEntity dataflowEntity) {
		log.debug("Saving dataflow '{}'.", dataflowEntity);

		// Save the dataflow.
		dataflowEntity = super.save(dataflowEntity);

		// Schedule dataflow in Kubernetes.
		DeploymentInfoDTO deploymentInfoDTO = createPodInfo(dataflowEntity);
		log.debug("Scheduling pod '{}' in Kubernetes.", deploymentInfoDTO);
		kubernetesResource.scheduleDeployment(deploymentInfoDTO);

		return dataflowEntity;
	}

	/**
	 * Retrieves the namespaces available in the Kubernetes cluster.
	 *
	 * @return The namespaces.
	 */
	@ErnPermission(category = DATAFLOW, operation = READ)
	public List<FormlySelectOption> getNamespaces() {
		return kubernetesResource.getNamespaces().stream()
			.map(namespace -> new FormlySelectOption(namespace, namespace)).toList();
	}

	/**
	 * Deletes a dataflow.
	 *
	 * @param dataflowId The ID of the dataflow to delete.
	 */
	@ErnPermission(category = DATAFLOW, operation = DELETE)
	public void delete(String dataflowId) {
		// Unschedule dataflow.
		DataflowEntity dataflow = findById(dataflowId);
		dataflow.setStatus(false);
		DeploymentInfoDTO deploymentInfoDTO = createPodInfo(dataflow);
		kubernetesResource.scheduleDeployment(deploymentInfoDTO);

		// Delete dataflow.
		super.delete(dataflow);
	}

	/**
	 * Creates a new dataflow entity.
	 *
	 * @param dataflowEntity The dataflow entity to save.
	 * @return The created dataflow entity.
	 */
	@ErnPermission(category = DATAFLOW, operation = CREATE)
	public DataflowEntity saveNew(DataflowEntity dataflowEntity) {
		return saveHandler(dataflowEntity);
	}

	/**
	 * Saves an updated dataflow entity.
	 *
	 * @param dataflowEntity The dataflow entity to save.
	 * @return The saved dataflow entity.
	 */
	@ErnPermission(category = DATAFLOW, operation = WRITE)
	public DataflowEntity saveUpdate(DataflowEntity dataflowEntity) {
		return saveHandler(dataflowEntity);
	}

	/**
	 * Finds dataflows.
	 *
	 * @param pageable     Representation of page, size, and sort search parameters.
	 * @param partialMatch Whether to do a partial match.
	 * @return The dataflows.
	 */
	@Override
	@ErnPermission(category = DATAFLOW, operation = READ)
	public Page<DataflowEntity> find(Pageable pageable, boolean partialMatch) {
		return super.find(pageable, partialMatch);
	}

	/**
	 * Finds a dataflow by ID.
	 *
	 * @param id The ID of the entity to find.
	 * @return The dataflow entity.
	 */
	@Override
	@ErnPermission(category = DATAFLOW, operation = READ)
	public DataflowEntity findById(String id) {
		return super.findById(id);
	}

	/**
	 * Checks if a Kubernetes deployment name is available.
	 *
	 * @param name      The name to check.
	 * @param namespace The namespace to check.
	 * @return Whether the deployment name is available.
	 */
	@ErnPermission(category = DATAFLOW, operation = READ)
	public boolean isDeploymentNameAvailable(String name, String namespace) {
		return kubernetesResource.isDeploymentNameAvailable(name, namespace);
	}
}
