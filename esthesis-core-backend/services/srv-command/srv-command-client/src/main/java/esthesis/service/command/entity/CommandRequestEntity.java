package esthesis.service.command.entity;

import esthesis.common.avro.CommandType;
import esthesis.common.avro.ExecutionType;
import esthesis.core.common.entity.BaseEntity;
import io.quarkus.mongodb.panache.common.MongoEntity;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.validation.constraints.NotEmpty;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

/**
 * Represents a command request to be executed by a device.
 * <p>
 * The serialised format of a command request is:
 * <pre>
 * [id] [commandType]{executionType} {command} {arguments}
 * </pre>
 * for example:
 * <pre>
 *  abc1234 cs hostname
 *  abc1234 cs ls -asl
 *  abc1234 ca update
 *  abc1234 r
 * </pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@MongoEntity(collection = "CommandRequest")
public class CommandRequestEntity extends BaseEntity {

	// A comma-separated list of hardware IDs to which the command should be sent.
	private String hardwareIds;

	// A comma-separated list of tag IDs to which the command should be sent.
	private String tags;

	// The type of execution for the command.
	@NotEmpty
	private CommandType commandType;

	// The type of execution for the command.
	@NotEmpty
	private ExecutionType executionType;

	// The command to be executed.
	private String command;

	// The arguments for the commands.
	private String arguments;

	// The command to be executed.
	private String description;

	// The time when the command was created.
	@NotEmpty
	private Instant createdOn = Instant.now();

	// The time when the command was dispatched. This indicates the time this command was queued in
	// Kafka.
	private Instant dispatchedOn;
}

