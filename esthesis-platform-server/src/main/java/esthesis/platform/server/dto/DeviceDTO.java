package esthesis.platform.server.dto;

import esthesis.platform.common.dto.BaseDTO;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class DeviceDTO extends BaseDTO {
  @NotNull
  private String deviceId;

  private String publicKey;

  private String privateKey;

  @NotNull
  private String state;

  @Singular
  private List<Long> tags;
}
