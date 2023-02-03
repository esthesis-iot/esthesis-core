package esthesis.services.campaign.impl.worker;

import esthesis.common.AppConstants.Campaign.Condition.Op;
import esthesis.common.AppConstants.Campaign.Condition.Type;
import esthesis.service.campaign.dto.CampaignConditionDTO;
import esthesis.service.campaign.dto.GroupDTO;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.services.campaign.impl.service.CampaignService;
import java.text.ChoiceFormat;
import java.time.Instant;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

@Slf4j
@ApplicationScoped
public class DateTimeWorker extends BaseWorker {

  @Inject
  CampaignService campaignService;

  public boolean dateTimeCondition(String campaignId, String groupExpression) {
    log.debug("dateTimeCondition, campaignId: {}, group: {}", campaignId, groupExpression);
    setStateDescription(campaignId, "Checking date/time condition.");

    // Get the campaign details, conditions, and devices for this campaign.
    CampaignEntity campaignEntity = campaignService.findById(campaignId);
    GroupDTO groupDTO = new GroupDTO(groupExpression);
    List<CampaignConditionDTO> conditions = getCondition(campaignEntity, groupDTO, Type.DATETIME);
    if (CollectionUtils.isEmpty(conditions)) {
      log.debug("No date/time condition found for campaign id '{}', group '{}'.", campaignId,
          groupExpression);
      return true;
    } else {
      log.debug("Found '{}' date/time {}.", conditions.size(),
          new ChoiceFormat("0#conditions|1#condition|1<conditions").format(conditions.size()));
    }

    boolean dateTimeCondition = true;
    for (CampaignConditionDTO condition : conditions) {
      log.debug("Checking date/time condition '{}'.", condition);
      if (condition.getOperation() == Op.BEFORE) {
        if (!Instant.now().isBefore(condition.getScheduleDate())) {
          dateTimeCondition = false;
        }
      } else if (condition.getOperation() == Op.AFTER) {
        if (!Instant.now().isAfter(condition.getScheduleDate())) {
          dateTimeCondition = false;
        }
      } else {
        log.warn("Unsupported date/time condition operation '{}', will be skipped.",
            condition.getOperation());
      }

      if (!dateTimeCondition) {
        log.debug("Date/time condition evaluation failed, not all devices satisfy condition "
                + "'{}'.",
            condition);
        break;
      }
    }

    return dateTimeCondition;
  }
}
