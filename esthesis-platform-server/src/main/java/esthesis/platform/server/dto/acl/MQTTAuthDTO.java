package esthesis.platform.server.dto.acl;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class MQTTAuthDTO {
  private String username;
  private String topic;
  private int  acc;
}
