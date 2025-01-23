package esthesis.service.device.entity;

import esthesis.common.util.EsthesisCommonConstants;
import esthesis.core.common.AppConstants;
import esthesis.core.common.entity.BaseEntity;
import esthesis.service.device.dto.DeviceKeyDTO;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

/**
 * An entity representing a device.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "Device")
public class DeviceEntity extends BaseEntity {

	@NotBlank
	@Length(min = 3, max = 512)
	@Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Only alphanumeric characters, hyphens, and underscores are allowed.")
	private String hardwareId;

	private AppConstants.Device.Status status;

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

	// The public, private, and certificate for the device.
	private DeviceKeyDTO deviceKey;

	// The type of the device.
	private EsthesisCommonConstants.Device.Type type;

}
