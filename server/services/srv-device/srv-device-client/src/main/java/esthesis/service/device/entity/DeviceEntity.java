package esthesis.service.device.entity;

import esthesis.common.AppConstants.DeviceStatus;
import esthesis.common.entity.BaseEntity;
import esthesis.service.device.dto.DeviceKeyDTO;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.Instant;
import java.util.List;
import javax.validation.constraints.NotBlank;
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
@MongoEntity(collection = "Device")
public class DeviceEntity extends BaseEntity {

  @NotBlank
  @Length(min = 1, max = 1024)
  private String hardwareId;

  private DeviceStatus status;

  // List of tag ids.
  private List<String> tags;

  private Instant lastSeen;

  private DeviceKeyDTO deviceKey;
}
