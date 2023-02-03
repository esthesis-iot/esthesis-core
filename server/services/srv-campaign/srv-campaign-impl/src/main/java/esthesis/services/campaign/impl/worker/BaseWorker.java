package esthesis.services.campaign.impl.worker;

import esthesis.common.AppConstants.Campaign.Condition.Type;
import esthesis.service.campaign.dto.CampaignConditionDTO;
import esthesis.service.campaign.dto.GroupDTO;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.services.campaign.impl.service.CampaignService;
import java.util.List;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class BaseWorker {

  @Inject
  CampaignService campaignService;

  protected List<CampaignConditionDTO> getCondition(CampaignEntity campaignEntity,
      GroupDTO groupDTO, Type type) {
    return campaignEntity.getConditions().stream()
        .filter(condition -> condition.getType() == type)
        .filter(condition -> condition.getGroup() == groupDTO.getGroup())
        .filter(condition -> condition.getStage() == groupDTO.getStage())
        .toList();
  }

  public void setStateDescription(String campaignId, String stateDescription) {
    CampaignEntity campaignEntity = campaignService.findById(campaignId);
    campaignEntity.setStateDescription(stateDescription);
    campaignService.save(campaignEntity);
  }
}
