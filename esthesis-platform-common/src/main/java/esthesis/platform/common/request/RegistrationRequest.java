package esthesis.platform.common.request;

import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class RegistrationRequest {
  // An id uniquely identifying this device in platform server.
  @NotNull
  private String deviceId;

  // TODO
  private String hmac;

  // Comma-separated list of tags. These tags need to already exist in platform server, otherwiser registration fails.
  private String tags;

  // An optional friendly name for this device.
  private String friendlyName;

}
