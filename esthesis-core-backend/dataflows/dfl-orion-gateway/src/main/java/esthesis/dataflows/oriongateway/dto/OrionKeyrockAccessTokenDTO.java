package esthesis.dataflows.oriongateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for representing an Orion Keyrock access token.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrionKeyrockAccessTokenDTO {

	@JsonProperty("access_token")
	private String accessToken;

	@JsonProperty("expires_in")
	private int expiresIn;


	@JsonProperty("refresh_token")
	private String refreshToken;

	@JsonProperty("token_type")
	private String tokenType;

	@JsonProperty("scope")
	private List<String> scope;

	// Manually set while retrieving the response
	private Instant expirationTime;

}
