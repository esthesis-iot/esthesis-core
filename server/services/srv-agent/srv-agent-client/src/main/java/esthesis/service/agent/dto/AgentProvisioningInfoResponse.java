package esthesis.service.agent.dto;

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
public class AgentProvisioningInfoResponse {

  // The id of the provisioning package.
  private String id;

  // The name of the provisioning package.
  private String name;

  // The version of the provisioning package.
  private String version;

  // The size (in bytes) of the provisioning package.
  private long size;

  // The SHA256 digest of the provisioning package.
  private String sha256;

  // The URL to download this provisioning package from.
  private String downloadUrl;

  // The download token to provide when downloading this provisioning package.
  private String downloadToken;

  // The filename of the provisioning package.
  private String filename;
}
