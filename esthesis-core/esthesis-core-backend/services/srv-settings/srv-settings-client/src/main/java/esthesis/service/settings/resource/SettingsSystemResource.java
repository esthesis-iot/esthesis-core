package esthesis.service.settings.resource;

import esthesis.common.AppConstants.NamedSetting;
import esthesis.service.settings.entity.SettingEntity;
import io.quarkus.oidc.client.reactive.filter.OidcClientRequestReactiveFilter;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import org.eclipse.microprofile.rest.client.annotation.RegisterProvider;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@Path("/api")
@RegisterRestClient(configKey = "SettingsSystemResource")
@RegisterProvider(OidcClientRequestReactiveFilter.class)
public interface SettingsSystemResource {

	@GET
	@Path("/v1/system/find/by-name/{name}")
	SettingEntity findByName(@PathParam("name") NamedSetting name);

}
