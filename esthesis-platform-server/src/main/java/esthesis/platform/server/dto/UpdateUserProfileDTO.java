package esthesis.platform.server.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import esthesis.common.device.dto.BaseDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
public class UpdateUserProfileDTO extends BaseDTO {

  private String fn;
  private String ln;
  private String oldPassword;
  private String newPassword1;
  private String newPassword2;
  private String newEmail;
  private String salt;
  private String email;
  private int status;
}
