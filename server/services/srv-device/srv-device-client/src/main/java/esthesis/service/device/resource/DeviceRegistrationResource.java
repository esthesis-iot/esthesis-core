package esthesis.service.device.resource;

import esthesis.service.device.dto.Device;
import esthesis.service.device.dto.DeviceRegistration;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import org.bouncycastle.operator.OperatorCreationException;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "DeviceRegistrationResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface DeviceRegistrationResource {

  @POST
  @Path("/v1/device/preregister")
  Response preregister(@Valid DeviceRegistration deviceRegistration)
  throws NoSuchAlgorithmException, IOException, OperatorCreationException,
         InvalidKeySpecException, NoSuchProviderException;

  @PUT
  @Path("/v1/device/activate/{hardwareId}")
  Device activatePreregisteredDevice(
      @PathParam(value = "hardwareId") String hardwareId);

}
