package esthesis.platform.backend.server.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@Accessors(chain = true)
public class UserProfileDTO {

  private String firstName;
  private String lastName;
  private String email;

}
