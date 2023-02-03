package esthesis.services.campaign.impl.worker;

import esthesis.common.AppConstants.Campaign.Condition.Type;
import esthesis.service.campaign.dto.CampaignConditionDTO;
import esthesis.service.campaign.dto.GroupDTO;
import esthesis.service.campaign.entity.CampaignEntity;
import esthesis.services.campaign.impl.service.CampaignDeviceMonitorService;
import esthesis.services.campaign.impl.service.CampaignService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

@Slf4j
@ApplicationScoped
public class CheckRateWorker extends BaseWorker {

  @Inject
  CampaignService campaignService;

  @Inject
  CampaignDeviceMonitorService campaignDeviceMonitorService;

  public boolean checkRate(String campaignId, String groupExpression) {
    log.debug("rateCondition, campaignId: {}, group: {}", campaignId, groupExpression);
    setStateDescription(campaignId, "Checking rate condition.");
    
    // Get the campaign details and parse the group expression.
    CampaignEntity campaignEntity = campaignService.findById(campaignId);
    GroupDTO groupDTO = new GroupDTO(groupExpression);

    // Get the requested rate number.
    List<CampaignConditionDTO> conditions = getCondition(campaignEntity, groupDTO, Type.SUCCESS);
    BigDecimal requestedRate;
    if (CollectionUtils.isEmpty(conditions)) {
      log.debug("No rate condition found for campaign id '{}', group '{}'.", campaignId,
          groupExpression);
      return true;
    } else if (conditions.size() > 1) {
      log.warn("Found '{}' rate conditions for campaign id '{}', group '{}', using the first one.",
          conditions.size(), campaignId, groupExpression);
      requestedRate = new BigDecimal(conditions.get(0).getValue());
    } else {
      log.debug("Found batch condition '{}' for campaign id '{}', group '{}'.",
          conditions.get(0), campaignId, groupExpression);
      requestedRate = new BigDecimal(conditions.get(0).getValue());
    }
    requestedRate = requestedRate.divide(new BigDecimal(100), 2, RoundingMode.FLOOR);

    // Compare the requested rate with the actual rate.
    BigDecimal actualRate = campaignDeviceMonitorService.checkRate(campaignId,
        groupDTO.getGroup());
    log.debug("Requested rate: '{}', actual rate: '{}'.", requestedRate, actualRate);

    return actualRate.compareTo(requestedRate) >= 0;
  }
}
