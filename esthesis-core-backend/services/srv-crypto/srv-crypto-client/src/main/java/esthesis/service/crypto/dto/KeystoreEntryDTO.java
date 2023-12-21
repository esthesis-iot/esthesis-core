package esthesis.service.crypto.dto;

import esthesis.common.AppConstants;
import esthesis.common.AppConstants.Keystore.Item.KeyType;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

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
