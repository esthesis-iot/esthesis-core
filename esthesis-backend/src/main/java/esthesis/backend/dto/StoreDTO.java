package esthesis.backend.dto;

import esthesis.common.device.dto.BaseDTO;
import javax.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Singular;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class StoreDTO extends BaseDTO {
  @NotNull
  private String name;
  @NotNull
  private String password;
  private boolean passwordForKeys;
  @Singular
  private List<Long> certCertificates;
  @Singular
  private List<Long> certCas;
  @Singular
  private List<Long> pkCertificates;
  @Singular
  private List<Long> pkCas;
}
