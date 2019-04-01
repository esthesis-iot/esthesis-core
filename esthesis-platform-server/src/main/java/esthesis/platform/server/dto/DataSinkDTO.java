package esthesis.platform.server.dto;

import esthesis.platform.common.dto.BaseDTO;
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
  private String factoryClass;

  @NotNull
  private String sinkType;

  @NotNull
  private boolean state;

  private String configuration;

  @NotNull
  private String name;
}
