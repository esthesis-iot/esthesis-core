package esthesis.service.application.entity;

import esthesis.common.entity.BaseEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "Application")
public class ApplicationEntity extends BaseEntity {

	@NotBlank
	@Length(min = 1, max = 255)
	private String name;

	@NotBlank
	@Length(min = 1, max = 4096)
	private String token;

	@NotNull
	private Boolean state;

	private Instant createdOn;
}
