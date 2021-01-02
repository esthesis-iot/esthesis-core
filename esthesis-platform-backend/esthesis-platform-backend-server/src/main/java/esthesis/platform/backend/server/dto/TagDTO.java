package esthesis.platform.backend.server.dto;

import esthesis.platform.backend.common.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class TagDTO extends BaseDTO {

  @Length(min = 3, max = 255)
  private String name;

}
