package esthesis.platform.server.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.extension.config.AppConstants.Generic;
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
@EqualsAndHashCode(callSuper=false)
public class ZookeeperConfigurationChangedEvent extends ApplicationEvent {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(ZookeeperConfigurationChangedEvent.class.getName());

  private long zookeeperServerId;
  private boolean stateBefore;
  private boolean stateAfter;
  private String nodeInitiatingChange;
  private boolean deleted;
  private static ObjectMapper mapper = new ObjectMapper();

  public ZookeeperConfigurationChangedEvent() {
    super(Generic.SYSTEM);
  }

  @Override
  public String toString() {
    try {
      return mapper.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      LOGGER.log(Level.SEVERE, "Could not serialize ZookeeperConfigurationChangedEvent.", e);
      return null;
    }
  }

  public byte[] toByteArray() {
    return toString().getBytes(StandardCharsets.UTF_8);
  }

  public static ZookeeperConfigurationChangedEvent fromByteArray(byte[] zookeeperConfigurationChangeEvent) {
    try {
      if (zookeeperConfigurationChangeEvent == null || zookeeperConfigurationChangeEvent.length == 0) {
        return null;
      } else {
        return mapper.readValue(zookeeperConfigurationChangeEvent, ZookeeperConfigurationChangedEvent.class);
      }
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Could not deserialize ZookeeperConfigurationChangedEvent.", e);
      return null;
    }
  }
}
