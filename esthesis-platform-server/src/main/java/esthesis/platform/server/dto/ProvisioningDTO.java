package esthesis.platform.server.dto;

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
  @Singular
  private List<Long> tags;
  @NotNull
  private String packageVersion;
  private long fileSize;
  private String fileName;
  private boolean signed;
  private boolean encrypted;
  // The signature of the unencrypted file.
  private String signaturePlain;

  // The signature of the encrypted file.
  private String signatureEncrypted;

  // The SHA256 digest of the unencrypted version of this provisioning package.
  private String sha256;
}
