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
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class ProvisioningDTO extends BaseDTO {

  @NotNull
  private String name;
  private String description;
  private boolean state;
  @Singular
  private List<Long> tags;
  @NotNull
  private String packageVersion;
  private long size;
  private String fileName;
  private boolean signed;
  private boolean encrypted;
}
