package esthesis.service.provisioning.impl;

import esthesis.common.banner.BannerUtil;
import esthesis.service.provisioning.impl.gridfs.GridFSDBMigration;
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
	GridFSDBMigration gridFSDBMigration;

	@SuppressWarnings("unused")
	void onStart(@Observes StartupEvent ev) {
		BannerUtil.showBanner(appName);
		gridFSDBMigration.checkMigration();
	}
}
