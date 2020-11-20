package esthesis.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder
public class WebSocketMessageDTO implements Serializable {
  // The payload to deliver.
  private String payload;

  // The topic on which the payload is delivered.
  private String topic;

}
