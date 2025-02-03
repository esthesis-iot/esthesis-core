package esthesis.service.settings.resource;

import esthesis.core.common.AppConstants.NamedSetting;
import esthesis.service.settings.entity.DevicePageFieldEntity;
import esthesis.service.settings.entity.SettingEntity;
import io.quarkus.oidc.token.propagation.AccessToken;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import java.util.List;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * A REST client for the settings service.
 */
@AccessToken
@Path("/api")
@RegisterRestClient(configKey = "SettingsResource")
public interface SettingsResource {

	/**
	 * Finds a setting by name.
	 *
	 * @param name The name of the setting to find.
	 * @return The setting, or null if not found.
	 */
	@GET
	@Path("/v1/find/by-name/{name}")
	SettingEntity findByName(@PathParam("name") NamedSetting name);

	/**
	 * Finds multiple settings entries by name.
	 *
	 * @param names A comma-separated list of names.
	 * @return The settings, or an empty list if none are found.
	 */
	@GET
	@Path("/v1/find/by-names/{names}")
	List<SettingEntity> findByNames(@PathParam("names") String names);

	/**
	 * Saves one or more settings.
	 *
	 * @param settingEntities The settings to save.
	 */
	@POST
	@Path("/v1")
	void save(@Valid SettingEntity... settingEntities);

	/**
	 * Finds all unique measurement names currently held in Redis cache.
	 *
	 * @return A list of unique measurement names.
	 */
	@GET
	@Path("/v1/find-measurement-names")
	List<String> findAllUniqueMeasurementNames();

	/**
	 * Gets the measurements that have been configured in settings for being displayed in the device
	 * page.
	 *
	 * @return The fields.
	 */
	@GET
	@Path("/v1/device-page-fields")
	List<DevicePageFieldEntity> getDevicePageFields();

	/**
	 * Saves the measurements that have been configured in settings for being displayed in the device
	 * page.
	 *
	 * @param fields The fields to save.
	 */
	@POST
	@Path("/v1/device-page-fields")
	void saveDevicePageFields(@Valid List<DevicePageFieldEntity> fields);
}
