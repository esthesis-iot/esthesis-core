package esthesis.common.device.control;

import esthesis.common.config.AppConstants;
import esthesis.common.config.AppConstants.Generic;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

@Data
@ToString
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class MqttControlCommand extends ApplicationEvent {

  @NotNull
  private long id;
  @NotNull
  private Instant sentOn;
  @NotNull
  private AppConstants.MqttCommand command;
  private byte[] commandPayload;

  public MqttControlCommand() {
    super(Generic.SYSTEM);
  }
}
