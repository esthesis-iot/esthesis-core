package esthesis.backend.nifi.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class NiFiTemplateDTO {

  private String id;
  private String name;
  private String description;
  private String groupId;
}
