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
  private String mqttTopicPing;

  @NotBlank
  @Length(max = 1024)
  private String mqttTopicTelemetry;

  @NotBlank
  @Length(max = 1024)
  private String mqttTopicMetadata;

  @NotBlank
  @Length(max = 1024)
  private String mqttTopicControlRequest;

  @NotBlank
  @Length(max = 1024)
  private String mqttTopicControlReply;

  @NotBlank
  @Length(max = 1024)
  private String kafkaTopicPing;

  @NotBlank
  @Length(max = 1024)
  private String kafkaTopicTelemetry;

  @NotBlank
  @Length(max = 1024)
  private String kafkaTopicMetadata;

  @NotBlank
  @Length(max = 1024)
  private String kafkaTopicControlRequest;

  @NotBlank
  @Length(max = 1024)
  private String kafkaTopicControlReply;
}
