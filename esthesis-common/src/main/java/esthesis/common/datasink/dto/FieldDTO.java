package esthesis.common.datasink.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode
public class FieldDTO {

  // The name of the field prefixed with the measurement, i.e. measurement.field, health.cpu.
  private String name;
  private String datatype;
  private boolean shown;
  private String label;
  private String formatter;
  private String valueHandler;
  private Object value;

  public FieldDTO(String name, String datatype) {
    this.name = name;
    this.datatype = datatype;
  }
}
