package esthesis.common.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bson.Document;

/**
 * Represents a command reply sent from a device.
 * <p>
 * The serialised format of a command reply is:
 * <pre>
 *  [correlationId] [success/failure] [output]
 * </pre>
 * for example:
 * <pre>
 *  abc1234 s Hello from the command
 * </pre>
 * or
 * <pre>
 *  abc1234 f File not found
 * </pre>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class CommandReply extends BaseDTO {

  // The CommandRequest to which this reply is related.
  private String correlationId;

  // The hardware ID to which this command was executed. This information can
  // already be extracted from the correlated CommandRequest, however it is
  // also included here for convenience.
  private String hardwareId;

  // Whether the command was executed successfully or execution resulted in
  // an error.
  private boolean success;

  // The output of the command.
  private String output;

  // The time when the reply was created.
  private Instant createdOn;

  /**
   * Returns a representation of this object as a BSON document. This is useful
   * in case Quarkus Panache is not available where this object is being used
   * (for example, in Dataflows).
   */
  public Document asDocument() {
    return new Document()
        .append("correlationId", correlationId)
        .append("hardwareId", hardwareId)
        .append("success", success)
        .append("output", output)
        .append("createdOn", createdOn);
  }
}
