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
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
public class VirtualizationDTO extends BaseDTO {
  @NotNull
  private String name;

  @NotNull
  private String ipAddress;

  @NotNull
  private int serverType;

  @NotNull
  private boolean state;

  @NotNull
  private int security;

  private Long certificate;

  @Singular
  private List<Long> tags;
}
