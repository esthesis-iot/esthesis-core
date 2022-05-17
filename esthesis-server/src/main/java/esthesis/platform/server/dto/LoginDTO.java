package esthesis.platform.server.dto;


import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class LoginDTO {
  private String email;
  private String password;
}
