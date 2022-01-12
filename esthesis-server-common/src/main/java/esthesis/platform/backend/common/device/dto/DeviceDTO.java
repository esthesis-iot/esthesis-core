package esthesis.platform.backend.common.device.dto;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import javax.validation.constraints.NotNull;

import esthesis.platform.backend.common.dto.BaseDTO;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class DeviceDTO extends BaseDTO {

  @NotNull
  @Schema(description = "The hardwareId of the device.")
  private String hardwareId;

  @NotNull
  @Schema(description = "The state of the device.")
  private String state;

  @Singular
  @ArraySchema(schema = @Schema(description = "Tags of the device"))
  private List<Long> tags;


  @Schema(description = "The public key to communicate with the device.")
  private String publicKey;
  @Schema(description = "The private key to communicate with the device.")
  private String privateKey;
  @Schema(description = "The certificate to communicate with the device.")
  private String certificate;
  @Schema(description = "Last time the device was seen by esthesis.")
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
