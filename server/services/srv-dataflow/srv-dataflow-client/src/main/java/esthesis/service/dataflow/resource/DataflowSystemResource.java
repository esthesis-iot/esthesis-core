package esthesis.service.dataflow.resource;

import esthesis.service.dataflow.dto.DataFlowMqttClientConfig;
import io.quarkus.oidc.client.reactive.filter.OidcClientRequestReactiveFilter;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "DataflowSystemResource")
@RegisterProvider(OidcClientRequestReactiveFilter.class)
public interface DataflowSystemResource {

  @GET
  @Path("/v1/dataflow-system/match-mqqt-server-by-tags")
  DataFlowMqttClientConfig matchMqttServerByTags(List<String> tags);
}
