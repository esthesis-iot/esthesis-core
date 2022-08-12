package esthesis.service.dataflow.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class DataFlowMqttClientConfig {

  private String url;

  private List<String> tags;

  @NotBlank
  @Length(max = 1024)
  private String topicPing;

  @NotBlank
  @Length(max = 1024)
  private String topicTelemetry;

  @NotBlank
  @Length(max = 1024)
  private String topicMetadata;

  @NotBlank
  @Length(max = 1024)
  private String topicControlRequest;

  @NotBlank
  @Length(max = 1024)
  private String topicControlReply;
}
