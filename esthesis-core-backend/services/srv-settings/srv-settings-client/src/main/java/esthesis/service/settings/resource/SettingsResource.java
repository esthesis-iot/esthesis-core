package esthesis.service.settings.resource;

import esthesis.common.AppConstants;
import esthesis.common.AppConstants.NamedSetting;
import esthesis.service.settings.entity.DevicePageFieldEntity;
import esthesis.service.settings.entity.SettingEntity;
import io.quarkus.oidc.token.propagation.reactive.AccessTokenRequestReactiveFilter;
import jakarta.annotation.security.RolesAllowed;
import jakarta.validation.Valid;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import java.util.List;
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
	@Path("/v1/find/by-name/{name}")
	@RolesAllowed(AppConstants.ROLE_USER)
	SettingEntity findByName(@PathParam("name") NamedSetting name);

	/**
	 * Finds multiple settings entries by name.
	 *
	 * @param names A comma-separated list of names.
	 */
	@GET
	@Path("/v1/find/by-names/{names}")
	@RolesAllowed(AppConstants.ROLE_USER)
	List<SettingEntity> findByNames(@PathParam("names") String names);

	/**
	 * Saves one or more settings.
	 *
	 */
	@POST
	@Path("/v1")
	@RolesAllowed(AppConstants.ROLE_USER)
	void save(@Valid SettingEntity... settingEntities);

	/**
	 * Finds all unique measurement names currently held in Redis cache.
	 *
	 * @return
	 */
	@GET
	@Path("/v1/find-measurement-names")
	@RolesAllowed(AppConstants.ROLE_USER)
	List<String> findAllUniqueMeasurementNames();

	/**
	 * Gets the measurements that have been configured in settings for being displayed in the device
	 * page.
	 *
	 * @return
	 */
	@GET
	@Path("/v1/device-page-fields")
	@RolesAllowed(AppConstants.ROLE_USER)
	List<DevicePageFieldEntity> getDevicePageFields();

	/**
	 * Saves the measurements that have been configured in settings for being displayed in the device
	 * page.
	 *
	 * @param fields The fields to save.
	 */
	@POST
	@Path("/v1/device-page-fields")
	@RolesAllowed(AppConstants.ROLE_USER)
	void saveDevicePageFields(@Valid List<DevicePageFieldEntity> fields);
}
