package esthesis.platform.backend.server.dto;

import esthesis.platform.backend.common.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotNull;
import java.time.Instant;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class DevicePageDTO extends BaseDTO {

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
