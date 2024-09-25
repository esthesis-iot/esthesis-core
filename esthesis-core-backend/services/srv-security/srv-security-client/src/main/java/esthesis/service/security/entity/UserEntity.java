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
@MongoEntity(collection = "User")
@EqualsAndHashCode(callSuper = true)
public class UserEntity extends BaseEntity {

  private String username;
  private String email;
  private String firstName;
  private String lastName;
  private String description;
  // References to group IDs.
  private List<String> groups;
  // Custom policies expressed as string.
  private List<String> policies;
}
