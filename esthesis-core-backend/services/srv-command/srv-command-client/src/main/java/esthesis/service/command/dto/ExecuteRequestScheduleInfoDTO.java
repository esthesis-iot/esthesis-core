package esthesis.service.command.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class ExecuteRequestScheduleInfoDTO {

  // The total number of devices against which this command is to be executed.
  private int devicesMatched = 0;

  // The number of devices to which the command was successfully scheduled for.
  // Note, successfully scheduling the execution of the command does not
  // constitute that the command was actually successfully executed on the
  // device's side.
  private int devicesScheduled = 0;

	// The correlation ID for this request
	private String correlationId;

}
