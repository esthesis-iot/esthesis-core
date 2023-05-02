package esthesis.service.device.resource;

import esthesis.service.device.dto.DeviceRegistrationDTO;
import esthesis.service.device.entity.DeviceAttributeEntity;
import esthesis.service.device.entity.DeviceEntity;
import io.quarkus.oidc.client.reactive.filter.OidcClientRequestReactiveFilter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import org.bouncycastle.operator.OperatorCreationException;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "DeviceSystemResource")
@RegisterProvider(OidcClientRequestReactiveFilter.class)
public interface DeviceSystemResource {

	@POST
	@Path("/v1/system/register")
	DeviceEntity register(DeviceRegistrationDTO deviceRegistration)
	throws IOException, InvalidKeySpecException, NoSuchAlgorithmException,
				 OperatorCreationException, NoSuchProviderException;

	@GET
	@Path("/v1/system/find/by-hardware-id")
	DeviceEntity findByHardwareId(@QueryParam("hardwareId") String hardwareId);

	@GET
	@Path("/v1/system/find/by-id")
	DeviceEntity findById(@QueryParam("esthesisId") String esthesisId);

	@GET
	@Path("/v1/system/public-key")
	String findPublicKey(@QueryParam("hardwareId") String hardwareId);

	@GET
	@Path("/v1/system/{esthesisId}/attributes-by-esthesis-id")
	List<DeviceAttributeEntity> getDeviceAttributesByEsthesisId(
		@PathParam("esthesisId") String esthesisId);

	@GET
	@Path("/v1/system/{esthesisHardwareId}/attributes-by-esthesis-hardware-id")
	List<DeviceAttributeEntity> getDeviceAttributesByEsthesisHardwareId(
		@PathParam("esthesisHardwareId") String esthesisHardwareId);

	@GET
	@Path("/v1/system/device-ids")
	List<String> getDeviceIds();
}
