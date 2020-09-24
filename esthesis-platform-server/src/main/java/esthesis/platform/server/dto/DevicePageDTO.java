package esthesis.platform.server.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.Instant;

@Data
@NoArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
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
