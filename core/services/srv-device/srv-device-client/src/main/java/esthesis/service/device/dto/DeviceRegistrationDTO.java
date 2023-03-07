package esthesis.service.device.dto;

import esthesis.common.AppConstants;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
@EqualsAndHashCode
public class DeviceRegistrationDTO {

  @NotBlank
  private String hardwareId;

  // The list of tag names this device supports.
  @Singular
  private List<String> tags;

  // The type of the device being registered.
  @NotNull
  private AppConstants.Device.Type type;

  // The optional registration secret, when the platform operates in that mode.
  private String registrationSecret;

  // A comma-separated list of key-value-type tuples in the form of:
  // key1=val1;type1,key2=val2;type2,etc.
  // The type of the attribute is optional and if not defined the system will try to determine
  // what is the most appropriate type to use. If the type is defined, it must be one of the
  // values provided by AppConstants.Device.Attribute.Type.
  private String attributes;
}
