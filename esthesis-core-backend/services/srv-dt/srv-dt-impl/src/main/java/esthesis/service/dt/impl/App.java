package esthesis.service.dt.impl;

import esthesis.common.banner.BannerUtil;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.ws.rs.core.Application;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.eclipse.microprofile.openapi.annotations.OpenAPIDefinition;
import org.eclipse.microprofile.openapi.annotations.info.Contact;
import org.eclipse.microprofile.openapi.annotations.info.Info;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;

/**
 * A component providing management services for the DT functionality.
 */
@ApplicationScoped
@OpenAPIDefinition(
	info = @Info(
		title = "esthesis - Digital Twin API",
		version = "",
		contact = @Contact(
			name = "esthesis",
			url = "https://esthes.is",
			email = "esthesis@eurodyn.com")),
	security = @SecurityRequirement(name = "X-ESTHESIS-DT-APP")
)
public class App extends Application {

	@ConfigProperty(name = "quarkus.application.name")
	String appName;

	void onStart(@Observes StartupEvent ev) {
		BannerUtil.showBanner(appName);
	}
}
