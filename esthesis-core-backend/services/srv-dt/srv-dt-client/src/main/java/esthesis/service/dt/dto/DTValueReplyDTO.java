package esthesis.service.dt.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Representation of a DT value reply.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class DTValueReplyDTO {

	String hardwareId;
	String category;
	String measurement;
	Object value;
	Instant recordedAt;
	String valueType;
}
