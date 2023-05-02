package esthesis.service.crypto.resource;

import esthesis.common.AppConstants;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.crypto.entity.CaEntity;
import esthesis.service.crypto.form.ImportCaForm;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import jakarta.validation.Valid;
import jakarta.ws.rs.BeanParam;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.MultipartForm;

@Path("/api/ca")
@RegisterRestClient(configKey = "CAResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface CAResource {

	@GET
	@Path("/v1/find")
	Page<CaEntity> find(@BeanParam Pageable pageable);

	@GET
	@Path("/v1/{id}")
	CaEntity findById(@PathParam("id") String id);

	@GET
	@Path("/v1/eligible-for-signing")
	List<CaEntity> getEligbleForSigning();

	@GET
	@Path("/v1/{id}/download")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	Response download(@PathParam("id") String caId, @QueryParam("type") AppConstants.KeyType type);

	@POST
	@Path("/v1/import")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	CaEntity importCa(@MultipartForm ImportCaForm input);

	@DELETE
	@Path("/v1/{id}")
	void delete(@PathParam("id") String id);

	@POST
	@Path("/v1")
	CaEntity save(@Valid CaEntity object);

	@GET
	@Path("/v1/{caId}/certificate")
	String getCACertificate(@PathParam("caId") String caId);
}
