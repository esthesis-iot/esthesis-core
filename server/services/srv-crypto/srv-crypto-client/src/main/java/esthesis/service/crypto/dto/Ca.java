package esthesis.service.crypto.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import esthesis.common.dto.BaseDTO;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.time.Instant;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class Ca extends BaseDTO {

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

  // The parent CA id.
  private ObjectId parentCaId;

  private int type;

}
