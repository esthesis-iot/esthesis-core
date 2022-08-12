package esthesis.service.registry.resource;

import esthesis.common.AppConstants;
import esthesis.service.registry.dto.RegistryEntry;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import java.util.List;
import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api/v1/registry")
@RegisterRestClient(configKey = "RegistryResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface RegistryResourceV1 {

//  @GET
//  @Path("/{id}")
//  RegistryEntry findById(@PathParam("id") ObjectId id);

  @GET
  @Path("/find/by-name/{name}")
  RegistryEntry findByName(@PathParam("name") AppConstants.Registry name);

  /**
   * Finds multiple registry entries by name.
   *
   * @param names A comma-separated list of names.
   */
  @GET
  @Path("/find/by-names/{names}")
  List<RegistryEntry> findByNames(@PathParam("names") String names);
  
  @POST
  void save(@Valid RegistryEntry... registryEntry);

//  @PostMapping("byNames")
//  @ReplyFilter("key,val")
//  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not save settings.")
//  public void saveMultiple(@Valid @RequestBody List<KeyValue> settings) {
//    settingsService.setVals(Generic.SYSTEM,
//        settings.stream().map(keyValue -> keyValue.getKey().toString()).collect(
//            Collectors.toList()),
//        settings.stream().map(keyValue -> keyValue.getValue() != null ?
//            keyValue.getValue().toString() : "").collect(Collectors.toList()),
//        Generic.SYSTEM);
//  }

//  @GetMapping(path = "fields", produces = MediaType.APPLICATION_JSON_VALUE)
//  @ExceptionWrapper(wrapper = QExceptionWrapper.class, logMessage = "Could not get fields.")
//  public List<DevicePageDTO> getFields() {
//    return devicePageService.findAll();
//  }
//
//  @PostMapping("fields")
//  public ResponseEntity saveFields(@Valid @RequestBody List<DevicePageDTO> fields) {
//    // Delete removed fields.
//    devicePageService.deleteByIdIn(
//        devicePageService.findAll().stream().map(DevicePageDTO::getId)
//            .filter(
//                f -> !fields.stream().map(DevicePageDTO::getId).collect(Collectors.toList()).contains(f))
//            .collect(Collectors.toList())
//    );
//
//    // Save all fields.
//    devicePageService.saveAll(fields);
//
//    return ResponseEntity.ok().build();
//  }
}
