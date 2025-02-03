package esthesis.service.infrastructure.entity;

import esthesis.core.common.entity.BaseEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

/**
 * InfrastructureMqttEntity entity.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "InfrastructureMqtt")
public class InfrastructureMqttEntity extends BaseEntity {

	@NotBlank
	@Length(max = 1024)
	private String name;

	@NotBlank
	@Length(min = 3, max = 1024)
	private String url;

	private boolean active;

	// List of tag ids.
	private List<String> tags;
}
