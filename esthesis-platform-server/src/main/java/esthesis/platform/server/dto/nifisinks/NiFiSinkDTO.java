package esthesis.platform.server.dto.nifisinks;

import esthesis.platform.server.dto.BaseDTO;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class NiFiSinkDTO extends BaseDTO {

  @NotNull
  private String name;

  @NotNull
  private String factoryClass;
  private int handler;
  private boolean state;
  private String configuration;
  private String customInfo;
  private String type;
  private String validationErrors;

}
