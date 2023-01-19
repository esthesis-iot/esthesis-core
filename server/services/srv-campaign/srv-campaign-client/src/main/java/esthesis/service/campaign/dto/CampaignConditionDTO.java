package esthesis.service.campaign.dto;

import esthesis.common.AppConstants;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class CampaignConditionDTO {

  private AppConstants.Campaign.Condition.Type type;
  private AppConstants.Campaign.Condition.Op operation;
  private AppConstants.Campaign.Condition.Stage stage;
  private int group;

  private String value;
  private String propertyName;
  private Boolean propertyIgnorable;
  private Instant scheduleDate;
}
