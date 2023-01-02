package esthesis.service.audit.entity;

import esthesis.common.AppConstants;
import esthesis.common.AppConstants.Audit;
import esthesis.common.entity.BaseEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.Instant;
import javax.validation.constraints.NotNull;
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
@MongoEntity(collection = "Audit")
public class AuditEntity extends BaseEntity {

  @NotNull
  private Instant createdOn;
  @NotNull
  @Length(max = 255)
  private String createdBy;
  @NotNull
  private Audit.Category category;
  @NotNull
  private AppConstants.Audit.Operation operation;
  @NotNull
  @Length(max = 4096)
  private String message;
  private String valueBefore;
  private String valueAfter;
}
