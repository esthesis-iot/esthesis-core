package esthesis.service.device.dto;

import esthesis.common.util.EsthesisCommonConstants;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.Accessors;
import org.hibernate.validator.constraints.Length;

/**
 * A representation of the data needed to register a device.
 */
@Data
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
@EqualsAndHashCode
public class DeviceRegistrationDTO {

	@NotBlank
	@Length(min = 3, max = 512)
	@Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Hardware ID must contain only alphanumeric characters, hyphens, and underscores.")
	private String hardwareId;

	// The list of tag names this device supports.
	@Singular
	private List<String> tags;

	// The type of the device being registered.
	@NotNull
	private EsthesisCommonConstants.Device.Type type;

	// The optional registration secret, when the platform operates in that mode.
	private String registrationSecret;

	// A comma-separated list of key-value-type tuples in the form of:
	// key1=val1;type1,key2=val2;type2,etc.
	// The type of the attribute is optional and if not defined the system will try to determine
	// what is the most appropriate type to use. If the type is defined, it must be one of the
	// values provided by AppConstants.Device.Attribute.Type.
	private String attributes;
}
