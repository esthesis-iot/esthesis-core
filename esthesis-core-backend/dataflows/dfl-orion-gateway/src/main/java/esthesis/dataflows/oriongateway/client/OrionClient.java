package esthesis.dataflows.oriongateway.client;

import esthesis.dataflow.common.RestClientExceptionMapper;
import esthesis.dataflows.oriongateway.dto.OrionQueryDTO;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import java.util.List;
import java.util.Map;
import org.eclipse.microprofile.faulttolerance.Retry;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * A REST client for the Orion Context Broker.
 */
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
	@Path("/ngsi-ld/v1/entities")
	@Retry(maxRetries = 3)
	void createEntity(String entityJson);

	@POST
	@Path("/ngsi-ld/v1/entityOperations/upsert")
	@Retry(maxRetries = 3)
	void createOrUpdateEntities(String entitiesJson);

	@POST
	@Path("/ngsi-ld/v1/entities/{entityId}/attrs")
	@Retry(maxRetries = 3)
	void appendAttributes(@PathParam("entityId") String entityId, String attributesJson);

	@PATCH
	@Path("/ngsi-ld/v1/entities/{entityId}/attrs")
	@Retry(maxRetries = 3)
	void setAttributes(@PathParam("entityId") String entityId, String attributesJson);

	@PATCH
	@Path("/ngsi-ld/v1/entities/{entityId}/attrs/{attrName}")
	@Retry(maxRetries = 3)
	void setAttribute(@PathParam("entityId") String entityId,
		@PathParam("attrName") String attrName, String attributeJson);

	@DELETE
	@Path("/ngsi-ld/v1/entities/{entityId}/attrs/{attrName}")
	@Retry(maxRetries = 3)
	void deleteAttribute(@PathParam("entityId") String entityId,
		@PathParam("attrName") String attrName);

	@DELETE
	@Path("/ngsi-ld/v1/entities/{entityId}")
	@Retry(maxRetries = 3)
	void deleteEntity(@PathParam("entityId") String entityId);

	@POST
	@Path("/ngsi-ld/v1/entityOperations/delete")
	@Retry(maxRetries = 3)
	void deleteEntities(List<String> entitiesId);


	@GET
	@Path("/ngsi-ld/v1/entities/")
	@Retry(maxRetries = 3)
	List<Map<String, Object>> query(@BeanParam OrionQueryDTO orionQueryDTO);

	@GET
	@Path("/ngsi-ld/v1/entities/{entityId}")
	@Retry(maxRetries = 3)
	Map<String, Object> getEntity(@PathParam("entityId") String entityId);
}
