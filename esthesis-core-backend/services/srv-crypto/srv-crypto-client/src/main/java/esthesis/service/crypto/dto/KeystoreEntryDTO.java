package esthesis.service.crypto.dto;

import esthesis.core.common.AppConstants;
import esthesis.core.common.AppConstants.Keystore.Item.KeyType;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * A representation of a keystore entry.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class KeystoreEntryDTO implements Serializable {

	@NotBlank
	private String id;

	@NotBlank
	private AppConstants.Keystore.Item.ResourceType resourceType;

	@NotBlank
	private List<KeyType> keyType;

	@NotBlank
	private String name;

	private String password;
}
