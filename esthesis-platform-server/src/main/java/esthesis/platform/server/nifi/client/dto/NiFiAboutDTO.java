package esthesis.platform.server.nifi.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class NiFiAboutDTO {

  private String version;
  private String title;
}
