package esthesis.service.agent.impl;

import esthesis.common.banner.BannerUtil;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * A component allowing agents to communicate with CORE.
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
