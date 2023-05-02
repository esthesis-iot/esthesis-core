package esthesis.dataflows.oriongateway.client;

import esthesis.dataflow.common.RestClientExceptionMapper;
import esthesis.dataflows.oriongateway.dto.OrionQueryDTO;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("")
@Produces("application/json")
@RegisterRestClient(configKey = "OrionClient")
@RegisterProvider(RestClientExceptionMapper.class)
public interface OrionClient {

	@GET
	@Path("/version")
	@Retry(maxRetries = 3)
	String getVersion();

	@POST
	@Path("/v2/entities")
	@Retry(maxRetries = 3)
	void createEntity(String json);

	@POST
	@Path("/v2/entities/{entityId}/attrs")
	@Retry(maxRetries = 3)
	void setAttribute(@PathParam("entityId") String entityId, String json);

	@DELETE
	@Path("/v2/entities/{entityId}/attrs/{attrName}")
	@Retry(maxRetries = 3)
	void deleteAttribute(@PathParam("entityId") String entityId,
		@PathParam("attrName") String attrName);

	@DELETE
	@Path("/v2/entities/{entityId}")
	@Retry(maxRetries = 3)
	void deleteEntity(@PathParam("entityId") String entityId);

	@POST
	@Path("/v2/op/query")
	@Retry(maxRetries = 3)
	List<Map<String, Object>> query(OrionQueryDTO orionQueryDTO);
}
