package esthesis.platform.backend.server.dto;

import esthesis.platform.backend.common.dto.BaseDTO;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DashboardDTO extends BaseDTO {

  private String userId;
  private boolean shared;
  private String name;
  private boolean defaultView;
}
