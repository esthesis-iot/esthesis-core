package esthesis.service.application.impl;

import esthesis.common.banner.BannerUtil;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * A component providing management services for the Applications functionality.
 */
@ApplicationScoped
public class App {

	@ConfigProperty(name = "quarkus.application.name")
	String appName;

	@SuppressWarnings("unused")
	void onStart(@Observes StartupEvent ev) {
		BannerUtil.showBanner(appName);
	}
}
