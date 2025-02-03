package esthesis.service.dataflow.entity;

import esthesis.core.common.entity.BaseEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

/**
 * Dataflow entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "Dataflow")
public class DataflowEntity extends BaseEntity {

	@NotBlank
	@Length(max = 256)
	private String type;

	private boolean status;

	@NotBlank
	@Length(min = 3, max = 47)
	@Pattern(regexp = "^[a-z0-9-]+$")
	private String name;

	@Length(max = 4096)
	private String description;

	@SuppressWarnings("java:S1948")
	private Map<String, Object> config;
}
