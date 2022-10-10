package esthesis.service.dataflow.dto;

import esthesis.common.dto.BaseDTO;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.Map;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class Dataflow extends BaseDTO {

  @NotBlank
  @Length(max = 256)
  private String type;

  @NotNull
  private boolean status;

  @NotBlank
  @Length(max = 1024)
  private String name;

  @Length(max = 4096)
  private String description;

  private Map<String, Object> config;
  private Map<String, Object> kubernetes;

}
