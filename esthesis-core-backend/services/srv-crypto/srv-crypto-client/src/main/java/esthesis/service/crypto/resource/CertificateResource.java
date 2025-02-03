package esthesis.service.crypto.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.crypto.entity.CertificateEntity;
import io.quarkus.oidc.token.propagation.AccessToken;
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
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

/**
 * REST client for the certificate service.
 */
@AccessToken
@Path("/api/certificate")
@RegisterRestClient(configKey = "CertificateResource")
public interface CertificateResource {

	/**
	 * Finds all certificates.
	 *
	 * @param pageable paging parameters.
	 * @return a page of certificates.
	 */
	@GET
	@Path("/v1/find")
	Page<CertificateEntity> find(@BeanParam Pageable pageable);

	/**
	 * Finds a certificate by its ID, without including information on its issuer.
	 *
	 * @param id the certificate ID.
	 * @return the certificate.
	 */
	@GET
	@Path("/v1/{id}")
	CertificateEntity findById(@PathParam("id") String id);

	/**
	 * Find a certificate by its ID, including information on its issuer.
	 *
	 * @param id the certificate ID.
	 * @return the certificate.
	 */
	@GET
	@Path("/v1/{id}/complete")
	CertificateEntity findByIdComplete(@PathParam("id") String id);

	/**
	 * Downloads a certificate.
	 *
	 * @param certId the certificate ID.
	 * @param type   the type of key to download.
	 * @return the certificate.
	 */
	@GET
	@Path("/v1/{id}/download")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	Response download(@PathParam("id") String certId,
		@QueryParam("type") AppConstants.KeyType type);

	/**
	 * Imports a certificate.
	 *
	 * @param certificateEntity the certificate entity.
	 * @param publicKey         the public key.
	 * @param privateKey        the private key.
	 * @param certificate       the certificate.
	 * @return the certificate being imported.
	 */
	@POST
	@Path("/v1/import")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	CertificateEntity importCertificate(
		@RestForm("dto") @PartType(MediaType.APPLICATION_JSON) CertificateEntity certificateEntity,
		@RestForm("public") FileUpload publicKey, @RestForm("private") FileUpload privateKey,
		@RestForm("certificate") FileUpload certificate);

	/**
	 * Deletes a certificate.
	 *
	 * @param id the certificate ID to delete.
	 */
	@DELETE
	@Path("/v1/{id}")
	void delete(@PathParam("id") String id);

	/**
	 * Saves a certificate.
	 *
	 * @param object the certificate entity.
	 * @return the certificate entity.
	 */
	@POST
	@Path("/v1")
	CertificateEntity save(@Valid CertificateEntity object);
}
