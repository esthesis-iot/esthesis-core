package esthesis.services.campaign.impl.worker;

import esthesis.common.AppConstants.Campaign.Condition.Op;
import esthesis.common.AppConstants.Campaign.Condition.Type;
import esthesis.common.exception.QMismatchException;
import esthesis.service.campaign.dto.CampaignConditionDTO;
import esthesis.service.campaign.dto.GroupDTO;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.services.campaign.impl.service.CampaignService;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@ApplicationScoped
public class PauseWorker extends BaseWorker {

  @Inject
  CampaignService campaignService;

  public int pauseCondition(String campaignId, String groupExpression) {
    log.debug("pauseCondition, campaignId: {}, group: {}", campaignId, groupExpression);
    setStateDescription(campaignId, "Checking pause condition.");
    int pauseCondition = -1;

    CampaignEntity campaignEntity = campaignService.findById(campaignId);
    GroupDTO groupDTO = new GroupDTO(groupExpression);
    List<CampaignConditionDTO> conditions = getCondition(campaignEntity, groupDTO, Type.PAUSE);
    if (conditions.size() > 1) {
      throw new QMismatchException("More than one pause conditions found for campaign id '{}', "
          + "group '{}'.", campaignId, groupExpression);
    } else if (conditions.size() == 1) {
      log.debug("Found condition '{}'.", conditions.get(0));
      CampaignConditionDTO condition = conditions.get(0);
      if (condition.getOperation() == Op.TIMER_MINUTES) {
        pauseCondition = Integer.valueOf(condition.getValue());
      } else if (condition.getOperation() == Op.FOREVER) {
        pauseCondition = 0;
      } else {
        throw new QMismatchException("Unsupported pause condition operation '{}'.",
            condition.getOperation());
      }
    } else {
      log.debug("No pause condition found for campaign id '{}', group '{}'.", campaignId,
          groupExpression);
    }

    return pauseCondition;
  }
}
