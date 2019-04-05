package esthesis.platform.server.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import esthesis.extension.config.AppConstants.Generic;
import lombok.Data;
import lombok.experimental.Accessors;
import org.springframework.context.ApplicationEvent;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

@Data
@Accessors(chain = true)
public class DataSinkConfigurationChangedEvent extends ApplicationEvent {

  // JUL reference.
  private static final Logger LOGGER = Logger.getLogger(DataSinkConfigurationChangedEvent.class.getName());

  private long dataSinkId;
  private boolean stateBefore;
  private boolean stateAfter;
  private String nodeInitiatingChange;
  private boolean deleted;
  private static ObjectMapper mapper = new ObjectMapper();

  public DataSinkConfigurationChangedEvent() {
    super(Generic.SYSTEM);
  }

  @Override
  public String toString() {
    try {
      return mapper.writeValueAsString(this);
    } catch (JsonProcessingException e) {
      LOGGER.log(Level.SEVERE, "Could not serialize DataSinkConfigurationChangedEvent.", e);
      return null;
    }
  }

  public byte[] toByteArray() {
    return toString().getBytes(StandardCharsets.UTF_8);
  }

  public static DataSinkConfigurationChangedEvent fromByteArray(byte[] dataSinkConfigurationChangedEvent) {
    try {
      if (dataSinkConfigurationChangedEvent == null || dataSinkConfigurationChangedEvent.length == 0) {
        return null;
      } else {
        return mapper.readValue(dataSinkConfigurationChangedEvent, DataSinkConfigurationChangedEvent.class);
      }
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Could not deserialize DataSinkConfigurationChangedEvent.", e);
      return null;
    }
  }
}
