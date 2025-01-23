package esthesis.service.audit.entity;

import esthesis.core.common.AppConstants;
import esthesis.core.common.entity.BaseEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

/**
 * Entity to represent an audit entry.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "Audit")
public class AuditEntity extends BaseEntity {

	@NotNull
	private Instant createdOn;
	@NotNull
	@Length(max = 255)
	private String createdBy;
	@NotNull
	private AppConstants.Security.Category category;
	@NotNull
	private AppConstants.Security.Operation operation;
	@NotNull
	@Length(max = 4096)
	private String message;
	private String valueIn;
	private String valueOut;
}
