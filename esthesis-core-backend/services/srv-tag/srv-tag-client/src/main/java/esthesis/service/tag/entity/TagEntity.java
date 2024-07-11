package esthesis.service.tag.entity;

import esthesis.common.entity.BaseEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
@MongoEntity(collection = "Tag")
public class TagEntity extends BaseEntity {

	@NotBlank
	@Length(min = 3, max = 255)
	@Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Only alphanumeric characters, hyphens, and underscores are allowed.")
	private String name;

	@Length(max = 2048)
	private String description;

}
