package esthesis.platform.backend.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import lombok.Data;
import io.swagger.v3.oas.annotations.Hidden;

import java.time.Instant;

@Data
public abstract class BaseDTO {

  private Long id;

  @Hidden
  @JsonProperty(access = Access.READ_ONLY)
  private Instant createdOn;

  @Hidden
  @JsonProperty(access = Access.READ_ONLY)
  private String createdBy;

  @Hidden
  @JsonProperty(access = Access.READ_ONLY)
  private Instant modifiedOn;

  @Hidden
  @JsonProperty(access = Access.READ_ONLY)
  private String modifiedBy;

}
