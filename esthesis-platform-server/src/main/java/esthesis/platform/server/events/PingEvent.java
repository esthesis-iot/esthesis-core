package esthesis.platform.server.events;

import esthesis.extension.common.config.AppConstants.Generic;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.context.ApplicationEvent;

@Data
@ToString
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class PingEvent extends ApplicationEvent {

  private String hardwareId;

  public PingEvent() {
    super(Generic.SYSTEM);
  }

  public PingEvent(String hardwareId) {
    super(Generic.SYSTEM);
    this.hardwareId = hardwareId;
  }
}
