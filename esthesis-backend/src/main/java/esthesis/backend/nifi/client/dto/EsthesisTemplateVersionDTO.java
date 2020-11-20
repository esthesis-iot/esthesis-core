package esthesis.backend.nifi.client.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EsthesisTemplateVersionDTO {

  private String name;
  private int major;
  private int minor;
  private int patch;
}
