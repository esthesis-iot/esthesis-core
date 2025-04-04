package esthesis.service.dataflow.resource;

import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.dataflow.dto.FormlySelectOption;
import esthesis.service.dataflow.entity.DataflowEntity;
import io.quarkus.oidc.token.propagation.common.AccessToken;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * REST client for managing dataflows.
 */
@AccessToken
@Path("/api")
@RegisterRestClient(configKey = "DataflowResource")
public interface DataflowResource {

	/**
	 * Find dataflows.
	 *
	 * @param pageable paging parameters.
	 * @return a page of dataflows.
	 */
	@GET
	@Path("/v1/find")
	Page<DataflowEntity> find(@BeanParam Pageable pageable);

	/**
	 * Find a dataflow by id.
	 *
	 * @param id the dataflow id.
	 * @return the dataflow.
	 */
	@GET
	@Path("/v1/{id}")
	DataflowEntity findById(@PathParam("id") String id);

	/**
	 * Delete a dataflow by id.
	 *
	 * @param id the dataflow id.
	 * @return the response.
	 */
	@DELETE
	@Path("/v1/{id}")
	Response delete(@PathParam("id") String id);

	/**
	 * Save a dataflow.
	 *
	 * @param dataflowEntity the dataflow to save.
	 * @return the saved dataflow.
	 */
	@POST
	@Path("/v1")
	@Produces("application/json")
	DataflowEntity save(@Valid DataflowEntity dataflowEntity);

	/**
	 * Get all kubernetes namespaces.
	 *
	 * @return a list of kubernetes namespaces.
	 */
	@GET
	@Path("/v1/namespaces")
	List<FormlySelectOption> getNamespaces();
}
