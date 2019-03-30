package esthesis.platform.datasink.common;

import static esthesis.platform.common.config.AppConstants.Generic.SYSTEM;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.context.ApplicationEvent;


@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
public abstract class MQTTDataEvent extends ApplicationEvent  {
  private String topic;
  private int id;
  private int qos;
  private boolean duplicate;
  private boolean retained;
  private byte[] payload;

  public MQTTDataEvent() {
    super(SYSTEM);
  }
}
