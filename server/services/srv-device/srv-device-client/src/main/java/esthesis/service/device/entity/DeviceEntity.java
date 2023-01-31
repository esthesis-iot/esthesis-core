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
//@JsonIgnoreProperties(value = {"registeredOn", "lastSeen", "createdOn"}, allowGetters = true)
public class DeviceEntity extends BaseEntity {

  @NotBlank
  @Length(min = 1, max = 1024)
  private String hardwareId;

  private DeviceStatus status;

  // List of tag ids.
  private List<String> tags;

  private Instant lastSeen;

  // The date the actual registration of a device took place. A device can be pre-created in
  // the system by an administrator, but the actual registration of the real device to the
  // platform may take place at any time in the future.
  private Instant registeredOn;

  // The date this device was first created in the system. For self-registering devices, this
  // date will be (almost) identical to the registeredOn date.
  private Instant createdOn;

  private DeviceKeyDTO deviceKey;
}
