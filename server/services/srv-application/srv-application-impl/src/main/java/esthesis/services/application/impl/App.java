package esthesis.services.application.impl;

import esthesis.common.banner.BannerUtil;
import io.quarkus.runtime.StartupEvent;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

@ApplicationScoped
public class App {

  void onStart(@Observes StartupEvent ev) {
    BannerUtil.showBanner("srv-application");
  }
}
