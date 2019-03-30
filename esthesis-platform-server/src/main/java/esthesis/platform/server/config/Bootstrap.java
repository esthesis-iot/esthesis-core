package esthesis.platform.server.config;

import esthesis.platform.server.service.ZookeeperService;
import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;

@Service
public class Bootstrap {

  private final ZookeeperService zookeeperService;

  public Bootstrap(ZookeeperService zookeeperService) {
    this.zookeeperService = zookeeperService;
  }

  @PostConstruct
  public void startup() {
    zookeeperService.connect();
  }
}