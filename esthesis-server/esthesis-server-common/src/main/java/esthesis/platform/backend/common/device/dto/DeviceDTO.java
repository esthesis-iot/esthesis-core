package esthesis.platform.backend.common.device.dto;

import esthesis.platform.backend.common.dto.BaseDTO;
import java.util.Objects;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class DeviceDTO extends BaseDTO {

  @NotNull
  private String hardwareId;

  @NotNull
  private String state;

  @Singular
  private List<Long> tags;

  private String publicKey;
  private String privateKey;
  private String certificate;
  private Instant lastSeen;

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    DeviceDTO deviceDTO = (DeviceDTO) o;
    return Objects.equals(hardwareId, deviceDTO.hardwareId) && Objects
      .equals(state, deviceDTO.state) && Objects.equals(tags, deviceDTO.tags)
      && Objects.equals(publicKey, deviceDTO.publicKey) && Objects
      .equals(privateKey, deviceDTO.privateKey) && Objects
      .equals(certificate, deviceDTO.certificate) && Objects
      .equals(lastSeen, deviceDTO.lastSeen);
  }

  @Override
  public int hashCode() {
    return Objects
      .hash(super.hashCode(), hardwareId, state, tags, publicKey, privateKey, certificate,
        lastSeen);
  }
}
