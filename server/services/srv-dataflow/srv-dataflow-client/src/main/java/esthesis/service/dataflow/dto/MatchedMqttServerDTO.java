package esthesis.service.dataflow.dto;

import esthesis.common.AppConstants.TagsAlgorithm;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Represents the details of an MQTT server that matches the requested tags. This class is sent as a
 * reply to registering device agents while trying to find out which MQTT server to connect to.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class MatchedMqttServerDTO {

  private String url;
  private TagsAlgorithm matchingAlgorithm;
  private List<String> tagsUsed;
}
