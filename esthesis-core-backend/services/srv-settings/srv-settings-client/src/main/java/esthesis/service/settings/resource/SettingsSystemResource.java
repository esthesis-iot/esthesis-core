package esthesis.service.settings.resource;

import esthesis.core.common.AppConstants.NamedSetting;
import esthesis.service.settings.entity.SettingEntity;
import io.quarkus.oidc.client.filter.OidcClientFilter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@OidcClientFilter
@RegisterRestClient(configKey = "SettingsSystemResource")
public interface SettingsSystemResource {

	@GET
	@Path("/v1/system/find/by-name/{name}")
	SettingEntity findByName(@PathParam("name") NamedSetting name);
}
