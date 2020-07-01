package esthesis.platform.server.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class NiFiDTO extends BaseDTO {

  @NotNull
  private boolean state;

  @NotNull
  private String name;
  @NotNull
  private String url;
  private String description;
  @JsonProperty(access = Access.READ_ONLY)
  private String wfVersion;
  @JsonProperty(access = Access.READ_ONLY)
  private Boolean synced;
  @JsonProperty(access = Access.READ_ONLY)
  private Instant lastChecked;
}
