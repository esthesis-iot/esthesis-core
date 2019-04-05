package esthesis.platform.server.events;

import esthesis.extension.config.AppConstants.Generic;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.ApplicationEvent;

@Data
@EqualsAndHashCode(callSuper=false)
public class ZookeeperConnectivityEvent extends ApplicationEvent {
  private EVENT_TYPE eventType;
  private Long zookeeperServerId;

  public enum EVENT_TYPE {
    CONNECTED, DISCONNECTED,
    CLUSTER_LEADER, CLUSTER_FOLLOWER
  }

  public ZookeeperConnectivityEvent(EVENT_TYPE eventType, Long zookeeperServerId) {
    super(Generic.SYSTEM);
    this.eventType = eventType;
    this.zookeeperServerId = zookeeperServerId;
  }

  public ZookeeperConnectivityEvent(EVENT_TYPE event_type) {
    this(event_type, null);
  }

  @Override
  public String toString() {
    return "ZookeeperEvent{" +
        "eventType=" + eventType +
        ", zookeeperServerId=" + zookeeperServerId +
        '}';
  }
}
