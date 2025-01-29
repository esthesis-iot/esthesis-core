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

	public DataflowEntity createDataflow(String dataflowName) {
		DataflowEntity dataflow = new DataflowEntity();
		dataflow.setName(dataflowName);
		dataflow.setType("test type");
		dataflow.setConfig(createDataflowConfig());
		dataflow.setStatus(true);
		return dataflow;
	}

	// Create a configuration map that matches the expected structure for the dataflow configuration.
	public Map<String, Object> createDataflowConfig() {

		Map<String, Object> kubernetesConfig = new HashMap<>();

		// Secrets configuration
		List<Map<String, String>> secrets = new ArrayList<>();
		secrets.add(Map.of(
			"name", "exampleSecret",
			"path", "/path/to/secret",
			"content", "exampleContent"
		));
		kubernetesConfig.put("secrets", secrets);

		// Other Kubernetes configuration details.
		kubernetesConfig.put("namespace", "test-namespace");
		kubernetesConfig.put("container-image-version", "1.0.0");
		kubernetesConfig.put("pods-min", 1);
		kubernetesConfig.put("pods-max", 5);
		kubernetesConfig.put("cpu-request", "500m");
		kubernetesConfig.put("cpu-limit", "1");
		kubernetesConfig.put("registry", "docker.registry.url");
		kubernetesConfig.put("env", "ENV_VAR1=value1\nENV_VAR2=value2");

		// Flattened map inclusion.
		Map<String, Object> config = new HashMap<>();
		config.put("kubernetes", kubernetesConfig);
		return config;
	}


	public void clearDatabase() {
		dataflowRepository.deleteAll();
	}

	public List<String> getNamespaces() {
		return List.of("test-namespace-1", "test-namespace-2", "test-namespace-3");
	}


	/**
	 * Mock a Pageable object with the specified parameters.
	 *
	 * @param page The page number being requested.
	 * @param size The size of the page.
	 * @return The mocked Pageable object.
	 */
	public Pageable makePageable(int page, int size) {

		// Mock the request URI and parameters.
		UriInfo uriInfo = Mockito.mock(UriInfo.class);
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
