package esthesis.backend.dto;

import esthesis.common.device.dto.BaseDTO;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class DataSinkDTO extends BaseDTO {

  @NotNull
  private String name;

  @NotNull
  private String factoryClass;

  private boolean metadataRead;

  private boolean telemetryRead;

  private boolean metadataWrite;

  private boolean telemetryWrite;

  private boolean state;

  private String configuration;

  public boolean getState() {
    return state;
  }

}
