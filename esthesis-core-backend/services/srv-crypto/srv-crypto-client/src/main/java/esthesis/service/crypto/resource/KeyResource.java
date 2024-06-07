package esthesis.service.crypto.resource;

import esthesis.common.AppConstants;
import esthesis.service.crypto.dto.CreateCertificateRequestDTO;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import java.io.IOException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import org.bouncycastle.operator.OperatorCreationException;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/key")
@RegisterRestClient(configKey = "KeyResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface KeyResource {

	@GET
	@Path("/v1/keypair")
	@RolesAllowed(AppConstants.ROLE_USER)
	KeyPair generateKeyPair()
	throws NoSuchAlgorithmException, NoSuchProviderException;

	@GET
	@Path("/v1/publicKeyToPEM")
	@RolesAllowed(AppConstants.ROLE_USER)
	String publicKeyToPEM(PublicKey publicKey) throws IOException;

	@GET
	@Path("/v1/privateKeyToPEM")
	@RolesAllowed(AppConstants.ROLE_USER)
	String privateKeyToPEM(PrivateKey keyPair) throws IOException;

	@GET
	@Path("/v1/certificate")
	@RolesAllowed(AppConstants.ROLE_USER)
	String generateCertificateAsPEM(CreateCertificateRequestDTO createCertificateRequestDTO)
	throws NoSuchAlgorithmException, InvalidKeySpecException, OperatorCreationException, IOException;
}
