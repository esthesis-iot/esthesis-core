package esthesis.service.device.resource;

import esthesis.common.AppConstants;
import esthesis.service.device.dto.DeviceRegistrationDTO;
import esthesis.service.device.entity.DeviceEntity;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import org.bouncycastle.operator.OperatorCreationException;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "DeviceRegistrationResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface DeviceRegistrationResource {

	/**
	 * Preregister devices in the platform. DeviceRegistrationDTO.hardwareId may content multiple
	 * devices in this case, separated by new lines.
	 *
	 * @param deviceRegistration
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws OperatorCreationException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchProviderException
	 */
	@POST
	@Path("/v1/preregister")
	@RolesAllowed(AppConstants.ROLE_USER)
	Response preregister(@Valid DeviceRegistrationDTO deviceRegistration)
	throws NoSuchAlgorithmException, IOException, OperatorCreationException,
				 InvalidKeySpecException, NoSuchProviderException;

	@PUT
	@Path("/v1/activate/{hardwareId}")
	@RolesAllowed(AppConstants.ROLE_USER)
	DeviceEntity activatePreregisteredDevice(
		@PathParam(value = "hardwareId") String hardwareId);
}
