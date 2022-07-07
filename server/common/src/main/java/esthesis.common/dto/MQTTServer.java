package esthesis.common.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class MQTTServer extends BaseDTO {

  @NotNull
  public String name;
  @NotNull
  public String ipAddress;
  @NotNull
  public boolean state;
  @Singular
  private List<Long> tags;

  public boolean getState() {
    return state;
  }
}


