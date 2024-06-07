package esthesis.service.crypto.resource;

import esthesis.common.AppConstants;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.crypto.entity.CertificateEntity;
import esthesis.service.crypto.form.ImportCertificateForm;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import jakarta.annotation.security.RolesAllowed;
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
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.MultipartForm;

@Path("/api/certificate")
@RegisterRestClient(configKey = "CertificateResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface CertificateResource {

	@GET
	@Path("/v1/find")
	@RolesAllowed(AppConstants.ROLE_USER)
	Page<CertificateEntity> find(@BeanParam Pageable pageable);

	@GET
	@Path("/v1/{id}")
	@RolesAllowed(AppConstants.ROLE_USER)
	CertificateEntity findById(@PathParam("id") String id);

	@GET
	@Path("/v1/{id}/complete")
	@RolesAllowed(AppConstants.ROLE_USER)
	CertificateEntity findByIdComplete(@PathParam("id") String id);

	@GET
	@Path("/v1/{id}/download")
	@RolesAllowed(AppConstants.ROLE_USER)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	Response download(@PathParam("id") String certId,
		@QueryParam("type") AppConstants.KeyType type);

	@POST
	@Path("/v1/import")
	@RolesAllowed(AppConstants.ROLE_USER)
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	CertificateEntity importCertificate(@MultipartForm ImportCertificateForm input);

	@DELETE
	@Path("/v1/{id}")
	@RolesAllowed(AppConstants.ROLE_USER)
	void delete(@PathParam("id") String id);

	@POST
	@Path("/v1")
	@RolesAllowed(AppConstants.ROLE_USER)
	CertificateEntity save(@Valid CertificateEntity object);

}
