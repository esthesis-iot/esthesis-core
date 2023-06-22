package esthesis.service.crypto.entity;

import esthesis.common.entity.BaseEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "Ca")
public class CaEntity extends BaseEntity {

	@NotNull
	@Size(max = 256)
	private String cn;

	@NotNull
	@Size(max = 256)
	private String name;

	private Instant issued;

	@NotNull
	private Instant validity;

	private String publicKey;

	private String privateKey;

	private String certificate;

	// The parent CA CN.
	private String parentCa;

	// The parent CA id.
	private ObjectId parentCaId;

}
