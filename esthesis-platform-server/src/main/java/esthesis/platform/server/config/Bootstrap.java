package esthesis.platform.server.config;

import esthesis.platform.server.cluster.datasinks.DataSinkManager;
import esthesis.platform.server.cluster.zookeeper.ZookeeperClientManager;
import esthesis.platform.server.datasinks.DataSinkScanner;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * A bootstrap class to allow component initialization after application has fully started and
 * all Beans are properly configured (in contrast to @{@link javax.annotation.PostConstruct}).
 * This is particularly useful in case a component needs to emit events during initialization
 * (otherwise events are lost since @{@link EventListener} annotations are not fully discovered).
 *
 * In addition, bootstraping all components in a well-defined sequence here allows greater
 * control over system's bootup times.
 */
@Component
public class Bootstrap {
  private final ZookeeperClientManager zookeeperClientManager;
  private final DataSinkScanner dataSinkScanner;
  private final DataSinkManager dataSinkManager;

  public Bootstrap(
    ZookeeperClientManager zookeeperClientManager,
    DataSinkScanner dataSinkScanner,
    DataSinkManager dataSinkManager) {
    this.zookeeperClientManager = zookeeperClientManager;
    this.dataSinkScanner = dataSinkScanner;
    this.dataSinkManager = dataSinkManager;
  }

  @EventListener
  public void applicationStarted(ContextRefreshedEvent contextRefreshedEvent) {
    zookeeperClientManager.connect();
    dataSinkScanner.scan();
    dataSinkManager.startDataSinks();
  }
}
