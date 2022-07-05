package esthesis.service.registry.dto;

import esthesis.common.dto.BaseDTO;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
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
@MongoEntity(collection = "Registry")
public class RegistryEntry extends BaseDTO {

  private String name;
  private String value;

  public String asString() {
    return String.valueOf(value);
  }

  public int asInt() {
    return Integer.parseInt(String.valueOf(value));
  }

  public long asLong() {
    return Long.parseLong(String.valueOf(value));
  }

  public boolean asBoolean() {
    return Boolean.parseBoolean(String.valueOf(value));
  }
  
  public boolean isTrue() {
    return value != null && asBoolean();
  }

  public boolean isFalse() {
    return value != null && !asBoolean();
  }

  public ObjectId asObjectId() {
    return new ObjectId(asString());
  }
}
