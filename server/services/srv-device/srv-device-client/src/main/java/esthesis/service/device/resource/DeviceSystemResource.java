package esthesis.service.device.resource;

import esthesis.service.device.dto.Device;
import esthesis.service.device.dto.DeviceRegistration;
import io.quarkus.oidc.client.reactive.filter.OidcClientRequestReactiveFilter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import org.bouncycastle.operator.OperatorCreationException;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "DeviceSystemResource")
@RegisterProvider(OidcClientRequestReactiveFilter.class)
public interface DeviceSystemResource {

  @POST
  @Path("/v1/device-system/register")
  Device register(DeviceRegistration deviceRegistration)
  throws IOException, InvalidKeySpecException, NoSuchAlgorithmException,
         OperatorCreationException, NoSuchProviderException;
}
