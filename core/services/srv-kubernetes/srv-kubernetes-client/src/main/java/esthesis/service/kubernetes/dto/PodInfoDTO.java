package esthesis.service.kubernetes.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class PodInfoDTO {

  private String name;
  private String image;
  private String version;
  private String namespace;
  private int minInstances;
  private int maxInstances;
  private String cpuRequest;
  private String cpuLimit;
  private boolean status;
  private String env;

  private Map<String, String> configuration;
}
