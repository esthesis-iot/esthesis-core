package esthesis.service.crypto.dto;

import esthesis.common.dto.BaseDTO;
import io.quarkus.runtime.annotations.RegisterForReflection;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.Accessors;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@RegisterForReflection
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class Store extends BaseDTO {

  @NotNull
  private String name;
  @NotNull
  private String password;
  private boolean passwordForKeys;
  @Singular
  private List<ObjectId> certCertificates;
  @Singular
  private List<ObjectId> certCas;
  @Singular
  private List<ObjectId> pkCertificates;
  @Singular
  private List<ObjectId> pkCas;
}
