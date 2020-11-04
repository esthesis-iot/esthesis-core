package esthesis.platform.server.dto;

import esthesis.common.device.dto.BaseDTO;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class TagDTO extends BaseDTO {

  @NotNull
  private String name;

}
