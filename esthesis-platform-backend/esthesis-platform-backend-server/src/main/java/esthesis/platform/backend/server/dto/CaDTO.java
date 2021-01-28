package esthesis.platform.backend.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import esthesis.platform.backend.common.dto.BaseDTO;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.Instant;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class CaDTO extends BaseDTO {

  @NotNull
  @Size(max = 256)
  private String cn;

  @JsonProperty(access = Access.READ_ONLY)
  private Instant issued;

  @NotNull
  private Instant validity;

  @JsonProperty(access = Access.READ_ONLY)
  private String publicKey;

  @JsonProperty(access = Access.READ_ONLY)
  private String privateKey;

  @JsonProperty(access = Access.READ_ONLY)
  private String certificate;

  // The parent CA CN.
  private String parentCa;

  private int type;

}
