package esthesis.platform.server.dto;

import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class StoreDTO extends BaseDTO {
  @NotNull
  private String name;
  @Singular
  private List<Long> certificates;
}
