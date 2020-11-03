package esthesis.common.device.control;

import esthesis.common.config.AppConstants.Generic;
import javax.validation.constraints.NotEmpty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.context.ApplicationEvent;

import java.time.Instant;

@Getter
@Setter
@ToString
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class ControlCommandRequest extends ApplicationEvent {

  // The unique identifier of this command.
  @NotEmpty
  private String id;

  // The time this command was created in esthesis.
  @NotEmpty
  private Instant createdOn;

  // The user issued this command.
  @NotEmpty
  private String createdBy;

  // ORM versioning.
  private long version = 0;

  // The operation this command is about (for example, PING, EXECUTE, etc.)
  @NotEmpty
  private String operation;

  // The arguments to be provided for the operation.
  private String args;

  // A description for this command.
  private String description;

  public ControlCommandRequest() {
    super(Generic.SYSTEM);
  }
}
