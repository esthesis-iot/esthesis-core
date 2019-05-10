package esthesis.platform.server.dto;

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

  private boolean metadata;

  private boolean telemetry;

  private boolean state;

  private String configuration;

  public boolean getState() {
    return state;
  }

}
