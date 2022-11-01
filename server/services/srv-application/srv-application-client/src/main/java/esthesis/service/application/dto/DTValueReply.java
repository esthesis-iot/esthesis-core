package esthesis.service.application.dto;

import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
public class DTValueReply {

  String hardwareId;
  String category;
  String measurement;
  Object value;
  Instant recordedAt;
  String valueType;
}
