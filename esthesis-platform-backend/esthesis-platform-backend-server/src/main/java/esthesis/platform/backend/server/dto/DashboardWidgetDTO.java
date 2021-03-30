package esthesis.platform.backend.server.dto;

import esthesis.platform.backend.common.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class DashboardWidgetDTO extends BaseDTO {

  private String type;
  private int gridCols;
  private int gridRows;
  private int gridX;
  private int gridY;
  private String configuration;
  private long dashboard;
  private int updateEvery;
}
