package esthesis.service.dataflow.dto;

import esthesis.common.AppConstants;
import esthesis.common.dto.BaseDTO;
import io.quarkus.runtime.annotations.RegisterForReflection;
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
  @Length(max = 1024)
  private String name;

  @NotBlank
  @Length(max = 4096)
  private String description;

  @NotNull
  private boolean status;

  @NotNull
  private AppConstants.Dataflow type;

  private String configuration;

  private Integer minPods;
  private Integer maxPods;
}
