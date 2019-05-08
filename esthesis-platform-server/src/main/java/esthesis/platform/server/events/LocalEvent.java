package esthesis.platform.server.events;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.extension.config.AppConstants.Generic;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.context.ApplicationEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class LocalEvent extends ApplicationEvent {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(LocalEvent.class.getName());

  @NotNull
  private LOCAL_EVENT_TYPE eventType;
  // A flag indicating that this event is an event that was initially captured as a cluster event.
  private boolean clusterEvent;
  private static ObjectMapper mapper = new ObjectMapper()
    .setSerializationInclusion(Include.NON_EMPTY);

  public enum LOCAL_EVENT_TYPE {
    CONFIGURATION_ZOOKEEPER,
    CONFIGURATION_MQTT,
    CONFIGURATION_DATASINK,
    CONNECTIVITY_ZOOKEEPER_CONNECTED,
    CONNECTIVITY_ZOOKEEPER_DISCONNECTED,
    CONNECTIVITY_ZOOKEEPER_LEADER,
    CONNECTIVITY_ZOOKEEPER_FOLLOWER,
  }

  public LocalEvent() {
    super(Generic.SYSTEM);
  }

  public LocalEvent(LOCAL_EVENT_TYPE eventType) {
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

  public static LocalEvent fromByteArray(byte[] platformEvent) {
    try {
      if (platformEvent == null || platformEvent.length == 0) {
        return null;
      } else {
        return mapper.readValue(platformEvent, LocalEvent.class);
      }
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Could not deserialize PlatformEvent.", e);
      return null;
    }
  }
}
