package esthesis.platform.server.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.Instant;

@Data
@NoArgsConstructor
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
public class CampaignConditionDTO extends BaseDTO {
  private Integer type;
  private Integer target;
  private Integer stage;
  private Instant scheduleDate;
  private String value;
  private Integer operation;
  private String propertyName;
}
