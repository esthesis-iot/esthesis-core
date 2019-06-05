package esthesis.platform.server.events;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.extension.common.config.AppConstants.Generic;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.context.ApplicationEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@ToString
public class ClusterEvent extends ApplicationEvent {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(ClusterEvent.class.getName());

  @NotNull
  private CLUSTER_EVENT_TYPE eventType;
  @NotNull
  private String emitterNode;
  private static ObjectMapper mapper = new ObjectMapper()
    .setSerializationInclusion(Include.NON_EMPTY);

  public enum CLUSTER_EVENT_TYPE {
    CONFIGURATION_ZOOKEEPER,
    CONFIGURATION_MQTT,
    CONFIGURATION_DATA_SINK,
  }

  public ClusterEvent() {
    super(Generic.SYSTEM);
  }

  public ClusterEvent(CLUSTER_EVENT_TYPE eventType) {
    super(Generic.SYSTEM);
    this.eventType = eventType;
  }

  @Override
  public String toString() {
    try {
      return mapper.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      LOGGER.log(Level.SEVERE, "Could not serialize PlatformEvent.", e);
      return null;
    }
  }

  public byte[] toByteArray() {
    return toString().getBytes(StandardCharsets.UTF_8);
  }

  public static ClusterEvent fromByteArray(byte[] platformEvent) {
    try {
      if (platformEvent == null || platformEvent.length == 0) {
        return null;
      } else {
        return mapper.readValue(platformEvent, ClusterEvent.class);
      }
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Could not deserialize PlatformEvent.", e);
      return null;
    }
  }
}
