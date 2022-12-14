package esthesis.service.dataflow.impl.docker;

import esthesis.service.dataflow.dto.DockerTagsDTO;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/v2")
@RegisterRestClient(configKey = "DockerClient")
public interface DockerClient {

  @GET
  @Retry(maxRetries = 3)
  @Path("/repositories/{image}/tags")
  DockerTagsDTO getTags(@PathParam("image") String image);
}
