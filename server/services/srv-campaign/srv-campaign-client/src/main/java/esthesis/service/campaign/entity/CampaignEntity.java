package esthesis.service.campaign.entity;

import esthesis.common.AppConstants;
import esthesis.common.entity.BaseEntity;
import esthesis.service.campaign.dto.CampaignConditionDTO;
import esthesis.service.campaign.dto.CampaignMemberDTO;
import io.quarkus.mongodb.panache.common.MongoEntity;
import java.time.Instant;
import java.util.List;
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
@MongoEntity(collection = "Campaign")
public class CampaignEntity extends BaseEntity {

  // Common command fields.
  private String name;
  private String description;
  private AppConstants.Campaign.Type type;
  private AppConstants.Campaign.State state;
  private Instant startedOn;
  private Instant terminatedOn;
  private List<CampaignConditionDTO> conditions;
  private List<CampaignMemberDTO> members;
  private String processInstanceId;
  private String stateDescription;

  // Campaign-type specific fields.
  private String commandName;
  private String commandArguments;
  private String commandExecutionType;
  private String provisioningPackageId;

  // Advanced settings.
  private String advancedDateTimeRecheckTimer;
  private String advancedPropertyRecheckTimer;
  private String advancedUpdateRepliesTimer;
  private String advancedUpdateRepliesFinalTimer;
}
