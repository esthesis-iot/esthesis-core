package esthesis.service.dataflow.impl.docker;

import esthesis.service.dataflow.dto.DockerTags;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/v2")
@RegisterRestClient(configKey = "DockerClient")
public interface DockerClient {

  @GET
  @Path("/repositories/{image}/tags")
  DockerTags getTags(@PathParam("image") String image);
}
