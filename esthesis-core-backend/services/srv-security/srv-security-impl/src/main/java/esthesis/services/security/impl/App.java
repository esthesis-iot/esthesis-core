package esthesis.services.security.impl;

import esthesis.core.common.banner.BannerUtil;
import esthesis.services.security.impl.service.SecurityUserService;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class App {
	@ConfigProperty(name = "quarkus.application.name")
	String appName;

	@Inject
	SecurityUserService securityUserService;

	@SuppressWarnings("java:S1172")
	void onStart(@Observes StartupEvent ev) {
		BannerUtil.showBanner(appName);
		securityUserService.createDefaultAdmin();
	}
}
