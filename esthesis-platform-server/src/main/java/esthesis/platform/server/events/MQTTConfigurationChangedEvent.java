package esthesis.platform.server.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.experimental.Accessors;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

@Data
@Accessors(chain = true)
public class MQTTConfigurationChangedEvent {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(MQTTConfigurationChangedEvent.class.getName());

  private long mqttServerId;
  private int stateBefore;
  private int stateAfter;
  private String nodeInitiatingChange;
  private boolean deleted;
  private static ObjectMapper mapper = new ObjectMapper();

  @Override
  public String toString() {
    try {
      return mapper.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      LOGGER.log(Level.SEVERE, "Could not serialize MQTTConfigurationChangedEvent.", e);
      return null;
    }
  }

  public byte[] toByteArray() {
    return toString().getBytes(StandardCharsets.UTF_8);
  }

  public static MQTTConfigurationChangedEvent fromByteArray(byte[] mqttConfigurationChangedEvent) {
    try {
      if (mqttConfigurationChangedEvent == null || mqttConfigurationChangedEvent.length == 0) {
        return null;
      } else {
        return mapper.readValue(mqttConfigurationChangedEvent, MQTTConfigurationChangedEvent.class);
      }
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Could not deserialize MQTTConfigurationChangedEvent.", e);
      return null;
    }
  }
}
