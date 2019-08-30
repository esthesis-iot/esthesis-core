package esthesis.platform.server.events;

import esthesis.common.config.AppConstants.Generic;
import esthesis.common.datasink.MQTTDataEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.context.ApplicationEvent;

@Data
@ToString
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class CommandReplyEvent extends ApplicationEvent {
  private MQTTDataEvent mqttDataEvent;

  public CommandReplyEvent() {
    super(Generic.SYSTEM);
  }

  public CommandReplyEvent(MQTTDataEvent mqttDataEvent) {
    super(Generic.SYSTEM);
    this.mqttDataEvent = mqttDataEvent;
  }
}
