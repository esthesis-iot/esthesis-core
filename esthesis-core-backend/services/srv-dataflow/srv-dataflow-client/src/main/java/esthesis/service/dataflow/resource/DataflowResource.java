package esthesis.service.dataflow.resource;

import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.dataflow.dto.DockerTagsDTO;
import esthesis.service.dataflow.dto.FormlySelectOption;
import esthesis.service.dataflow.entity.DataflowEntity;
import io.quarkus.oidc.token.propagation.AccessToken;
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

@AccessToken
@Path("/api")
@RegisterRestClient(configKey = "DataflowResource")
public interface DataflowResource {

	@GET
	@Path("/v1/find")
	Page<DataflowEntity> find(@BeanParam Pageable pageable);

	@GET
	@Path("/v1/{id}")
	DataflowEntity findById(@PathParam("id") String id);

	@DELETE
	@Path("/v1/{id}")
	Response delete(@PathParam("id") String id);

	@POST
	@Path("/v1")
	@Produces("application/json")
	DataflowEntity save(@Valid DataflowEntity dataflowEntity);

	@GET
	@Path("/v1/docker-tags/{dflType}")
	DockerTagsDTO getImageTags(@PathParam("dflType") String dflType);

	@GET
	@Path("/v1/namespaces")
	List<FormlySelectOption> getNamespaces();
}
