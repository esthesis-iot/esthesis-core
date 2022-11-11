package esthesis.common.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.Instant;
import java.util.List;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
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
@EqualsAndHashCode(callSuper = true)
public class CommandRequest extends BaseDTO {

  // The command to be executed.
  private String command;

  // The arguments for the commands.
  private List<String> arguments;

  // The type of execution for the command (see AppConstants.COMMAND_EXECUTION_TYPE_*).
  @NotEmpty
  private String executionType;

  // The type of execution for the command (see AppConstant.COMMAND_FAMILY_TYPE_*).
  @NotEmpty
  private String commandType;

  // The time when the command was created.
  @NotEmpty
  private Instant createdOn;
}
