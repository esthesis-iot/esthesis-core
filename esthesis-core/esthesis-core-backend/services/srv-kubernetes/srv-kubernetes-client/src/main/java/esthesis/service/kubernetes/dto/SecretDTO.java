package esthesis.service.kubernetes.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.Accessors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class SecretDTO {

	// The name of the secret.
	private String name;

	// The entries of the secret.
	@Singular
	private List<SecretEntryDTO> entries;
}
