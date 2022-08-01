package esthesis.service.device.dto;

import esthesis.common.dto.BaseDTO;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.Instant;
import java.util.List;
import javax.validation.constraints.NotBlank;
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
public class Device extends BaseDTO {

  @NotBlank
  @Length(min = 1, max = 1024)
  private String hardwareId;

  private String state;

  private List<String> tags;

  private String publicKey;

  private String privateKey;

  private String certificate;

  private Instant lastSeen;

  private DeviceKey deviceKey;
}
