package esthesis.common.datasink;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import esthesis.common.config.AppConstants.Generic;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.context.ApplicationEvent;

@Data
@ToString
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@JsonIgnoreProperties(ignoreUnknown = true)
public class MQTTDataEvent extends ApplicationEvent {

  private String topic;
  private boolean mutable;
  private byte[] payload;
  private int qos;
  private boolean retained;
  private boolean dup;
  // This is an id generated during reception and does not map to the MQTT message id.
  private String id;
  // The hardware ID of the device for which this MQTT data event was generated. This is derived
  // from the topic name, e.g. /topic/hardwareid.
  private String hardwareId;

  public MQTTDataEvent() {
    super(Generic.SYSTEM);
  }
}
