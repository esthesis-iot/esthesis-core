package esthesis.platform.backend.common.device;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
public class RegistrationRequest implements GenericRequest {

  // Comma-separated list of tags.
  private String tags;

  // An indicator that the device expects the registration reply from the platform to be encrypted.
  private boolean repliesEncrypted;

  // An indicator that the device expects the registration reply from the platform to be signed.
  private boolean repliesSigned;
}
