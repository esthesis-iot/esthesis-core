package esthesis.platform.backend.server.dto;

import esthesis.platform.backend.common.dto.BaseDTO;
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
public class ProvisioningDTO extends BaseDTO {

  @NotNull
  private String name;
  private String description;
  private boolean state;
  @Singular
  private List<Long> tags;
  @NotNull
  private String packageVersion;
  private long fileSize;
  private String fileName;
  // The SHA256 digest of the provisioning package.
  private String sha256;
}
