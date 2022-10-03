package esthesis.service.dataflow.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class DockerTag {

  private String name;
  @JsonProperty("tag_status")
  private String status;
  @JsonProperty("last_updated")
  private Instant lastUpdated;
}
