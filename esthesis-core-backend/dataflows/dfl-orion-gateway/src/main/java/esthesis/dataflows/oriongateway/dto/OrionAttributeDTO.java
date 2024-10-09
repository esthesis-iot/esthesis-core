package esthesis.dataflows.oriongateway.dto;

import esthesis.common.data.DataUtils.ValueType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrionAttributeDTO {

  private String name;
  private String value;
  private ValueType type;
  private boolean maintainedByEsthesis;
}
