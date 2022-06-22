package esthesis.services.device.resource;

import esthesis.dto.Device;
import esthesis.services.device.service.DeviceService;
import java.util.List;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import org.eclipse.microprofile.jwt.JsonWebToken;

@Path("/api/v1/device")
@RequestScoped
public class DeviceResource {

  @Inject
  DeviceService deviceService;

  @Inject
  JsonWebToken jwt;

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String hello(HttpHeaders headers) {

    // Iterate over request headers
    for (String key : headers.getRequestHeaders().keySet()) {
      System.out.println(key + ": " + headers.getRequestHeaders().get(key));
    }

    System.out.println(deviceService.test());

    return "V5: Hello from RESTEasy Reactive";
  }

  @GET
  @Path("all")
  public List<Device> findAll() {
    return deviceService.findAll();
  }

  @POST
  public Device createDevice() {
    return deviceService.createDevice();
  }

}
