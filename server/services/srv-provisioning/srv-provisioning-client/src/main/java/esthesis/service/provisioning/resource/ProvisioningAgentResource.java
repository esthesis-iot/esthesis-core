package esthesis.service.provisioning.resource;

import esthesis.service.provisioning.entity.ProvisioningPackageEntity;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "ProvisioningAgentResource")
public interface ProvisioningAgentResource {

  @GET
  @Path("/v1/provisioning-agent/find/{hardwareId}")
  ProvisioningPackageEntity find(@PathParam("hardwareId") String hardwareId);
}
