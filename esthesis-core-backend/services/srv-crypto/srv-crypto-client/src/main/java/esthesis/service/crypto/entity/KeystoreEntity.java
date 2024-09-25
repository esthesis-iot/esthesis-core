package esthesis.service.crypto.entity;

import esthesis.core.common.entity.BaseEntity;
import esthesis.service.crypto.dto.KeystoreEntryDTO;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "Keystore")
public class KeystoreEntity extends BaseEntity {

	@NotBlank
	private String name;

	private String description;

	private String password;

	private List<KeystoreEntryDTO> entries;

	@NotNull
	private Integer version;

	@NotNull
	private String type;
}
