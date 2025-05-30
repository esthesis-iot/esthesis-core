package esthesis.service.dt.impl.openapi;

import io.quarkus.smallrye.openapi.OpenApiFilter;
import io.smallrye.openapi.api.models.PathsImpl;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.microprofile.openapi.OASFilter;
import org.eclipse.microprofile.openapi.models.OpenAPI;
import org.eclipse.microprofile.openapi.models.PathItem;
import org.eclipse.microprofile.openapi.models.Paths;

/**
 * Customises the OpenAPI paths by replacing "/api/v1/" with "/api/dt/v1/" which is necessary to
 * work with  proxy and API definitions setups.
 */
@Slf4j
@OpenApiFilter(OpenApiFilter.RunStage.BUILD)
public class OpenApiRuntimeFilter implements OASFilter {

	@Override
	public void filterOpenAPI(OpenAPI openAPI) {
		if (Objects.nonNull(openAPI.getPaths())) {
			Paths paths = openAPI.getPaths();
			Map<String, PathItem> newPathItems = paths.getPathItems().entrySet().stream().collect(
				Collectors.toMap(entry -> entry.getKey().replace("/api/v1/", "/api/dt/v1/"),
					Map.Entry::getValue));

			Paths adjustedPaths = new PathsImpl();
			adjustedPaths.setPathItems(newPathItems);

			openAPI.setPaths(adjustedPaths);
		}
		OASFilter.super.filterOpenAPI(openAPI);
	}
}
