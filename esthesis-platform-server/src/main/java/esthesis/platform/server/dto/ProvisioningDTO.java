package esthesis.platform.server.dto;

import javax.persistence.OneToMany;
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
  private boolean defaultIP;
  @Singular
  @OneToMany
  private List<TagDTO> tags;
  @NotNull
  private String version;
  private String fileId;
  private long fileSize;
}
