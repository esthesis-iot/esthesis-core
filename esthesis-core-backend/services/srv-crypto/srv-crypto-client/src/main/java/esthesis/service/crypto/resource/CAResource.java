package esthesis.service.crypto.resource;

import esthesis.core.common.AppConstants;
import esthesis.service.common.paging.Page;
import esthesis.service.common.paging.Pageable;
import esthesis.service.crypto.entity.CaEntity;
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
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

/**
 * REST client for the CA service.
 */
@AccessToken
@Path("/api/ca")
@RegisterRestClient(configKey = "CAResource")
public interface CAResource {

	/**
	 * Find all CAs.
	 *
	 * @param pageable paging parameters.
	 * @return a page of CAs.
	 */
	@GET
	@Path("/v1/find")
	Page<CaEntity> find(@BeanParam Pageable pageable);

	/**
	 * Finds multiple CAs by their ids.
	 *
	 * @param id A comma-separated list of ids.
	 * @return A list of CAs with the given ids.
	 */
	@GET
	@Path("/v1/find/by-ids")
	List<CaEntity> findByIds(@QueryParam("ids") String id);

	/**
	 * Finds a CAs by its cn.
	 *
	 * @param cn A CN of a CA.
	 * @return A CAs with the CN.
	 */
	@GET
	@Path("/v1/find/by-cn")
	CaEntity findByCn(@QueryParam("cn") String cn);

	/**
	 * Find a CA by ID, without its properties. Use this method to get the basic information of a CA.
	 *
	 * @param id the ID of the CA.
	 * @return a CA.
	 */
	@GET
	@Path("/v1/{id}")
	CaEntity findById(@PathParam("id") String id);

	/**
	 * Find a CA by ID with all its properties, including the public and private keys.
	 *
	 * @param id the ID of the CA.
	 * @return a CA.
	 */
	@GET
	@Path("/v1/{id}/complete")
	CaEntity findByIdComplete(@PathParam("id") String id);

	/**
	 * Get the list of CAs that are eligible for signing.
	 *
	 * @return a list of CAs.
	 */
	@GET
	@Path("/v1/eligible-for-signing")
	List<CaEntity> getEligbleForSigning();

	/**
	 * Download the public key, private key, or certificate of a CA.
	 *
	 * @param caId the ID of the CA.
	 * @param type the type of the key to download.
	 * @return the key as a file.
	 */
	@GET
	@Path("/v1/{id}/download")
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	Response download(@PathParam("id") String caId, @QueryParam("type") AppConstants.KeyType type);

	/**
	 * Import a CA.
	 *
	 * @param caEntity    the CA entity.
	 * @param publicKey   the public key file.
	 * @param privateKey  the private key file.
	 * @param certificate the certificate file.
	 * @return the imported CA.
	 */
	@POST
	@Path("/v1/import")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	CaEntity importCa(@RestForm("dto") @PartType(MediaType.APPLICATION_JSON) CaEntity caEntity,
		@RestForm("public") FileUpload publicKey, @RestForm("private") FileUpload privateKey,
		@RestForm("certificate") FileUpload certificate);

	/**
	 * Deletes a CA.
	 *
	 * @param id the ID of the CA to delete.
	 */
	@DELETE
	@Path("/v1/{id}")
	void delete(@PathParam("id") String id);

	/**
	 * Saves a CA.
	 *
	 * @param object the CA to save.
	 * @return the saved CA.
	 */
	@POST
	@Path("/v1")
	CaEntity save(@Valid CaEntity object);

	/**
	 * Finds the certificate of a CA.
	 *
	 * @param caId the ID of the CA.
	 * @return the certificate of the CA.
	 */
	@GET
	@Path("/v1/{caId}/certificate")
	String getCACertificate(@PathParam("caId") String caId);
}
