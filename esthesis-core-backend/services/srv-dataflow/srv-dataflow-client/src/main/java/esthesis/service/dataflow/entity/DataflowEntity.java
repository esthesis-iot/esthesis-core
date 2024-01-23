package esthesis.service.dataflow.entity;

import esthesis.common.entity.BaseEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
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
@MongoEntity(collection = "Dataflow")
public class DataflowEntity extends BaseEntity {

	@NotBlank
	@Length(max = 256)
	private String type;

	private boolean status;

	@NotBlank
	@Length(max = 1024)
	private String name;

	@Length(max = 4096)
	private String description;

	private Map<String, Object> config;
}
