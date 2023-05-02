package esthesis.services.infrastructure.impl;

import esthesis.common.banner.BannerUtil;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class App {

	void onStart(@Observes StartupEvent ev) {
		BannerUtil.showBanner("srv-infrastructure");
	}
}
