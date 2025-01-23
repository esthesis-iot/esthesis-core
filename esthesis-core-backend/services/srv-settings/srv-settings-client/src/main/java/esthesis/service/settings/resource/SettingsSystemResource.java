package esthesis.service.settings.resource;

import esthesis.core.common.AppConstants.NamedSetting;
import esthesis.service.settings.entity.SettingEntity;
import io.quarkus.oidc.client.filter.OidcClientFilter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

/**
 * A REST client for the settings system service, when accessed by SYSTEM OIDC clients.
 */
@Path("/api")
@OidcClientFilter
@RegisterRestClient(configKey = "SettingsSystemResource")
public interface SettingsSystemResource {

	/**
	 * Finds a setting by name.
	 *
	 * @param name The name of the setting to find.
	 * @return The setting, or null if not found.
	 */
	@GET
	@Path("/v1/system/find/by-name/{name}")
	SettingEntity findByName(@PathParam("name") NamedSetting name);
}
