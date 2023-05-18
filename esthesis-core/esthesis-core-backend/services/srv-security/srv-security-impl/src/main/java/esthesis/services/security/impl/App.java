package esthesis.services.security.impl;

import esthesis.common.banner.BannerUtil;
import esthesis.services.security.impl.service.SecurityUserService;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class App {

	@Inject
	SecurityUserService securityUserService;

	void onStart(@Observes StartupEvent ev) {
		BannerUtil.showBanner("srv-security");
		securityUserService.createDefaultAdmin();
	}
}
