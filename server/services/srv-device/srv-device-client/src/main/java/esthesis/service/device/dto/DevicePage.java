package esthesis.service.device.dto;

import esthesis.common.dto.BaseDTO;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.Instant;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class DevicePage extends BaseDTO {

  @NotNull
  private String measurement;
  @NotNull
  private String field;
  @NotNull
  private String datatype;
  private boolean shown;
  private String label;
  private String formatter;
  private String valueHandler;
  private Object value;
  private Instant lastUpdatedOn;
}
