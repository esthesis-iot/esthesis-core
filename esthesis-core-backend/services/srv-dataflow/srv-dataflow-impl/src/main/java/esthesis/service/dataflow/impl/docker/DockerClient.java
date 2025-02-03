package esthesis.service.dataflow.impl.docker;

import esthesis.service.dataflow.dto.DockerTagsDTO;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for managing Docker images.
 */
@Path("/v2")
@RegisterRestClient(configKey = "DockerClient")
public interface DockerClient {

	/**
	 * Get tags for an image.
	 *
	 * @param image the image name.
	 * @return the tags.
	 */
	@GET
	@Retry(maxRetries = 3)
	@Path("/repositories/{image}/tags")
	DockerTagsDTO getTags(@PathParam("image") String image);
}
