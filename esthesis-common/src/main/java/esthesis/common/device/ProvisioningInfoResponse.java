package esthesis.common.device;

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
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class ProvisioningInfoResponse extends GenericResponse {
  private long id;
  private String description;
  private long fileSize;
  private String fileName;
  private String name;
  private String packageVersion;

  // The signature is either the one of the plain or the encrypted file based on the value of
  // Setting.Provisioning.ENCRYPTION.
  private String signature;

  // The SHA256 digest of the unencrypted version of this provisioning package.
  private String sha256;
}
