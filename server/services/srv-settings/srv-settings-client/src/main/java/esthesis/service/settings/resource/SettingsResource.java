package esthesis.service.settings.resource;

import esthesis.common.AppConstants.NamedSetting;
import esthesis.service.settings.dto.DevicePageField;
import esthesis.service.settings.dto.Setting;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import java.util.List;
import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "SettingsResource")
@RegisterProvider(AccessTokenRequestReactiveFilter.class)
public interface SettingsResource {

  /**
   * Finds a setting by name.
   *
   * @param name The name of the setting.
   * @param name The name of the setting to find.
   */
  @GET
  @Path("/v1/settings/find/by-name/{name}")
  Setting findByName(@PathParam("name") NamedSetting name);

  /**
   * Finds multiple settings entries by name.
   *
   * @param names A comma-separated list of names.
   */
  @GET
  @Path("/v1/settings/find/by-names/{names}")
  List<Setting> findByNames(@PathParam("names") String names);

  /**
   * Saves one or more settings.
   *
   * @param settings The settings to save.
   */
  @POST
  @Path("/v1/settings")
  void save(@Valid Setting... settings);

  /**
   * Finds all unique measurement names currently held in Redis cache.
   *
   * @return
   */
  @GET
  @Path("/v1/settings/find-measurement-names")
  List<String> findAllUniqueMeasurementNames();

  /**
   * Gets the measurements that have been configured in settings for being
   * displayed in the device page.
   *
   * @return
   */
  @GET
  @Path("/v1/settings/device-page-fields")
  List<DevicePageField> getDevicePageFields();

  /**
   * Saves the measurements that have been configured in settings for being
   * displayed in the device page.
   *
   * @param fields The fields to save.
   */
  @POST
  @Path("/v1/settings/device-page-fields")
  void saveDevicePageFields(@Valid List<DevicePageField> fields);
}
