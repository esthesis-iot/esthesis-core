package esthesis.service.security.entity;

import esthesis.core.common.entity.BaseEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "Group")
public class GroupEntity extends BaseEntity {

  private String name;
  private String description;
  // References to role IDs
  private List<String> roles;
  // Custom policies, expressed as string.
  private List<String> policies;
}
