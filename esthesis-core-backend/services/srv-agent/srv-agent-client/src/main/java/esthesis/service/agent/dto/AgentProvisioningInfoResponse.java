package esthesis.service.agent.dto;

import esthesis.core.common.AppConstants.Provisioning;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Represents the response to a request for provisioning package information.
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Accessors(chain = true)
public class AgentProvisioningInfoResponse {

  // The id of the provisioning package.
	@NotEmpty
  private String id;

  // The name of the provisioning package.
	@NotEmpty
  private String name;

  // The version of the provisioning package.
	@NotEmpty
  private String version;

	// The type of provisioning package.
	@NotEmpty
	private Provisioning.Type type;

  // The size (in bytes) of the provisioning package (available only for INTERNAL packages).
  private long size;

	// The URL to download this provisioning package from, available only for EXTERNAL packages.
	// INTERNAL packages will be downloaded by the provisioning URL provided as part of the
	// registration process of the agent.
	private String downloadUrl;

  // The SHA256 digest of the provisioning package.
  private String sha256;

  // The download token to provide when downloading this provisioning package, is secure
	// provisioning is enabled.
  private String downloadToken;
}
