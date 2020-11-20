package esthesis.backend.nifi.client.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EsthesisTemplateDTO {

  // The Id of the template in the available templates repository.
  private String templateId;
  // For instantiated tamplates this is the Id of the flow group generated for this template.
  private String flowGroupId;
  @NotNull
  private String name;
  @NotNull
  private int versionMajor;
  @NotNull
  private int versionMinor;
  @NotNull
  private int versionPatch;
}
