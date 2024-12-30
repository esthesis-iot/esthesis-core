package esthesis.service.dataflow.impl.service;

import esthesis.service.common.paging.Pageable;
import esthesis.service.dataflow.entity.DataflowEntity;
import esthesis.service.dataflow.impl.repository.DataflowRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.UriInfo;
import org.mockito.Mockito;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;

@ApplicationScoped
public class TestHelper {

	@Inject
	DataflowRepository dataflowRepository;

	public void createDataflow() {
		DataflowEntity dataflow = new DataflowEntity();
		dataflow.setName("test dataflow");
		dataflow.setType("test type");
		dataflow.setConfig(createDataflowConfig());
		dataflow.setStatus(true);
		dataflowRepository.persist(dataflow);
	}

	// Create a configuration map that matches the expected structure for the dataflow configuration
	public Map<String, Object> createDataflowConfig() {
		// Kubernetes-specific configuration
		Map<String, Object> kubernetesConfig = new HashMap<>();

		// Secrets configuration
		List<Map<String, String>> secrets = new ArrayList<>();
		secrets.add(Map.of(
			"name", "exampleSecret", // Aligning with SECRET_NAME
			"path", "/path/to/secret", // Aligning with SECRET_PATH
			"content", "exampleContent" // Aligning with SECRET_CONTENT
		));
		kubernetesConfig.put("secrets", secrets);

		// Other Kubernetes configuration details
		kubernetesConfig.put("namespace", "test-namespace");
		kubernetesConfig.put("container-image-version", "1.0.0");
		kubernetesConfig.put("pods-min", 1);
		kubernetesConfig.put("pods-max", 5);
		kubernetesConfig.put("cpu-request", "500m");
		kubernetesConfig.put("cpu-limit", "1");
		kubernetesConfig.put("registry", "docker.registry.url");
		kubernetesConfig.put("env", "ENV_VAR1=value1\nENV_VAR2=value2");

		// Flattened map inclusion
		Map<String, Object> config = new HashMap<>();
		config.put("kubernetes", kubernetesConfig);
		return config;
	}


	public void clearDatabase() {
		dataflowRepository.deleteAll();
	}

	public List<DataflowEntity> findAllDataflowEntity() {
		return dataflowRepository.listAll();
	}

	public List<String> getNamespaces() {
		return List.of("test-namespace-1", "test-namespace-2", "test-namespace-3");
	}

	public DataflowEntity findOneDataflowEntity() {
		return dataflowRepository.findAll().firstResult();
	}

	/**
	 * Helper method to create a Pageable object with the specified parameters
	 */
	public Pageable makePageable(int page, int size) {

		// Create a mock of UriInfo
		UriInfo uriInfo = Mockito.mock(UriInfo.class);

		// Define the behavior of the mock
		when(uriInfo.getRequestUri()).thenReturn(URI.create("http://localhost:8080/find?page=" + page + "&size=" + size));
		when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());

		Pageable pageable = new Pageable();
		pageable.setPage(page);
		pageable.setSize(size);
		pageable.setSort("");
		pageable.setUriInfo(uriInfo);
		return pageable;
	}
}
